package ru.sportclub.service;

import ru.sportclub.exception.BusinessException;
import ru.sportclub.exception.EntityNotFoundException;
import ru.sportclub.model.TrainingRegistration.RegistrationStatus;
import ru.sportclub.model.TrainingRegistration;
import ru.sportclub.repository.MemberRepository;
import ru.sportclub.repository.TrainingRegistrationRepository;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class TrainingRegistrationService {
    private final TrainingRegistrationRepository repository;
    private final MemberRepository memberRepository;

    public TrainingRegistrationService(TrainingRegistrationRepository repository, MemberRepository memberRepository) {
        this.repository = repository;
        this.memberRepository = memberRepository;
    }

    public TrainingRegistration create(TrainingRegistration registration) {
        validate(registration, false);
        return repository.save(registration);
    }

    public List<TrainingRegistration> findAll() { return repository.findAll(); }

    public TrainingRegistration findById(long id) {
        return repository.findById(id).orElseThrow(() -> new EntityNotFoundException("Запись не найдена: " + id));
    }

    public TrainingRegistration update(TrainingRegistration registration) {
        TrainingRegistration current = findById(registration.getId());
        validateStatusTransition(current.getStatus(), registration.getStatus());
        validate(registration, true);
        return repository.update(registration);
    }

    public void delete(long id) {
        TrainingRegistration registration = findById(id);
        if (registration.getStatus() == RegistrationStatus.COMPLETED) {
            throw new BusinessException("Завершенную запись нельзя удалить");
        }
        repository.deleteById(id);
    }

    public List<TrainingRegistration> searchByTrainingName(String query) {
        String normalized = query.toLowerCase();
        return findAll().stream().filter(item -> item.getTrainingName().toLowerCase().contains(normalized)).toList();
    }

    public List<TrainingRegistration> searchByTrainer(String query) {
        String normalized = query.toLowerCase();
        return findAll().stream().filter(item -> item.getTrainerName().toLowerCase().contains(normalized)).toList();
    }

    public List<TrainingRegistration> filterByStatus(RegistrationStatus status) {
        return findAll().stream().filter(item -> item.getStatus() == status).toList();
    }

    public List<TrainingRegistration> filterByCategory(String category) {
        return findAll().stream().filter(item -> item.getCategory().equalsIgnoreCase(category)).toList();
    }

    public List<TrainingRegistration> sortByDate() {
        return findAll().stream().sorted(Comparator.comparing(TrainingRegistration::getTrainingDate)
                .thenComparing(TrainingRegistration::getStartTime)).toList();
    }

    public List<TrainingRegistration> sortByDurationDescending() {
        return findAll().stream().sorted(Comparator.comparing(TrainingRegistration::getDurationMinutes).reversed()).toList();
    }

    public Map<String, Long> statistics() {
        return findAll().stream().collect(Collectors.groupingBy(item -> item.getStatus().name(), Collectors.counting()));
    }

    private void validate(TrainingRegistration registration, boolean allowCompleted) {
        var member = memberRepository.findById(registration.getMemberId())
                .orElseThrow(() -> new BusinessException("Участник не существует: " + registration.getMemberId()));
        if (!member.isActive()) {
            throw new BusinessException("Нельзя записать неактивного участника");
        }
        if (registration.getTrainingName() == null || registration.getTrainingName().isBlank()) {
            throw new BusinessException("Название тренировки обязательно");
        }
        if (registration.getTrainerName() == null || registration.getTrainerName().isBlank()) {
            throw new BusinessException("ФИО тренера обязательно");
        }
        if (registration.getTrainingDate().isBefore(LocalDate.now())) {
            throw new BusinessException("Дата тренировки не может быть в прошлом");
        }
        if (registration.getDurationMinutes() < 30 || registration.getDurationMinutes() > 240) {
            throw new BusinessException("Продолжительность должна быть от 30 до 240 минут");
        }
        if (repository.existsAtTime(registration.getMemberId(), registration.getTrainingDate(), registration.getStartTime(), registration.getId())) {
            throw new BusinessException("Участник уже записан на тренировку в это время");
        }
        if (!allowCompleted && registration.getStatus() == RegistrationStatus.COMPLETED) {
            throw new BusinessException("Новую запись нельзя сразу создать завершенной");
        }
    }

    private void validateStatusTransition(RegistrationStatus current, RegistrationStatus requested) {
        if (current == RegistrationStatus.COMPLETED) {
            throw new BusinessException("Завершенную запись нельзя редактировать");
        }
        if (current == RegistrationStatus.CANCELLED) {
            throw new BusinessException("Отмененную запись нельзя редактировать");
        }
        if (requested == RegistrationStatus.COMPLETED && current != RegistrationStatus.CONFIRMED) {
            throw new BusinessException("Завершить можно только подтвержденную запись");
        }
    }
}
