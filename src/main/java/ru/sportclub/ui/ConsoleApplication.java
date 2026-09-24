package ru.sportclub.ui;

import ru.sportclub.exception.BusinessException;
import ru.sportclub.exception.DataAccessException;
import ru.sportclub.exception.EntityNotFoundException;
import ru.sportclub.model.*;
import ru.sportclub.model.Member.MembershipType;
import ru.sportclub.model.TrainingRegistration.RegistrationStatus;
import ru.sportclub.service.*;
import ru.sportclub.util.ExcelExporter;

import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class ConsoleApplication {
    private final Scanner scanner = new Scanner(System.in);
    private final MemberService memberService;
    private final TrainingRegistrationService registrationService;
    private final ExcelExporter exporter = new ExcelExporter();

    public ConsoleApplication(MemberService memberService, TrainingRegistrationService registrationService) {
        this.memberService = memberService;
        this.registrationService = registrationService;
    }

    public void run() {
        boolean running = true;
        while (running) {
            printMainMenu();
            try {
                if (!scanner.hasNextLine()) {
                    break;
                }
                switch (scanner.nextLine().trim()) {
                    case "1" -> membersMenu();
                    case "2" -> registrationsMenu();
                    case "3" -> searchMenu();
                    case "4" -> filterMenu();
                    case "5" -> printStatistics();
                    case "6" -> export();
                    case "7" -> printTables();
                    case "0" -> running = false;
                    default -> System.out.println("Неизвестный пункт меню.");
                }
            } catch (BusinessException | EntityNotFoundException | DataAccessException exception) {
                System.out.println("Ошибка: " + exception.getMessage());
            } catch (Exception exception) {
                System.out.println("Ошибка ввода или базы данных: " + exception.getMessage());
            }
        }
        System.out.println("Работа завершена.");
    }

    private void printMainMenu() {
        System.out.println("\n===== СПОРТИВНЫЙ КЛУБ =====");
        System.out.println("1. Участники");
        System.out.println("2. Записи на тренировки");
        System.out.println("3. Поиск");
        System.out.println("4. Фильтрация и сортировка");
        System.out.println("5. Статистика");
        System.out.println("6. Экспорт в Excel");
        System.out.println("7. Вывести таблицы базы данных");
        System.out.println("0. Выход");
        System.out.print("Выберите действие: ");
    }

    private void membersMenu() {
        while (true) {
            System.out.println("\n===== УЧАСТНИКИ =====");
            System.out.println("1. Добавить участника  2. Все участники  3. Найти по ID  4. Изменить активность  5. Удалить  0. Назад");
            if (!scanner.hasNextLine()) {
                return;
            }
            switch (scanner.nextLine().trim()) {
                case "1" -> memberService.create(new Member(input("ФИО: "), input("Телефон: "), input("Email: "), membershipType()));
                case "2" -> printMembers(memberService.findAll());
                case "3" -> System.out.println(memberService.findById(inputLong("ID: ")));
                case "4" -> {
                    Member member = memberService.findById(inputLong("ID: "));
                    member.setActive(!member.isActive());
                    memberService.update(member);
                    System.out.println("Статус участника изменен.");
                }
                case "5" -> memberService.delete(inputLong("ID: "));
                case "0" -> { return; }
                default -> System.out.println("Неизвестный пункт меню.");
            }
        }
    }

    private void registrationsMenu() {
        while (true) {
            System.out.println("\n===== ЗАПИСИ НА ТРЕНИРОВКИ =====");
            System.out.println("1. Создать запись  2. Все записи  3. По ID  4. Изменить запись  5. Удалить  0. Назад");
            if (!scanner.hasNextLine()) {
                return;
            }
            switch (scanner.nextLine().trim()) {
                case "1" -> registrationService.create(newRegistration());
                case "2" -> printRegistrations(registrationService.findAll());
                case "3" -> System.out.println(registrationService.findById(inputLong("ID: ")));
                case "4" -> updateRegistration();
                case "5" -> registrationService.delete(inputLong("ID: "));
                case "0" -> { return; }
                default -> System.out.println("Неизвестный пункт меню.");
            }
        }
    }

    private TrainingRegistration newRegistration() {
        return new TrainingRegistration(inputLong("ID участника: "), input("Название тренировки: "),
                input("Тренер: "), LocalDate.parse(input("Дата (ГГГГ-ММ-ДД): ")),
                LocalTime.parse(input("Время (ЧЧ:ММ): ")), inputInt("Продолжительность минут: "),
                RegistrationStatus.PLANNED, input("Категория: "));
    }

    private void updateRegistration() {
        TrainingRegistration current = registrationService.findById(inputLong("ID: "));
        TrainingRegistration updated = new TrainingRegistration(current.getId(), current.getMemberId(), current.getMemberName(),
                input("Название тренировки: "), input("Тренер: "),
                LocalDate.parse(input("Дата (ГГГГ-ММ-ДД): ")),
                LocalTime.parse(input("Время (ЧЧ:ММ): ")), inputInt("Продолжительность минут: "),
                status(), input("Категория: "), current.getCreatedAt());
        registrationService.update(updated);
    }

    private void searchMenu() {
        System.out.println("1. По названию тренировки  2. По тренеру");
        List<TrainingRegistration> result = scanner.nextLine().trim().equals("1")
                ? registrationService.searchByTrainingName(input("Фрагмент названия: "))
                : registrationService.searchByTrainer(input("Фрагмент ФИО тренера: "));
        printRegistrations(result);
    }

    private void filterMenu() {
        System.out.println("1. По статусу  2. По категории  3. Сортировка по дате  4. По длительности");
        switch (scanner.nextLine().trim()) {
            case "1" -> printRegistrations(registrationService.filterByStatus(status()));
            case "2" -> printRegistrations(registrationService.filterByCategory(input("Категория: ")));
            case "3" -> printRegistrations(registrationService.sortByDate());
            case "4" -> printRegistrations(registrationService.sortByDurationDescending());
            default -> System.out.println("Неизвестный пункт.");
        }
    }

    private void printStatistics() {
        List<TrainingRegistration> all = registrationService.findAll();
        Map<String, Long> byStatus = registrationService.statistics();
        List<Member> members = memberService.findAll();
        long activeMembers = members.stream().filter(Member::isActive).count();
        System.out.println("Всего участников: " + members.size());
        System.out.println("Активных участников: " + activeMembers);
        System.out.println("Всего записей: " + all.size());
        System.out.println("Подтвержденных: " + byStatus.getOrDefault("CONFIRMED", 0L));
        System.out.println("Запланированных: " + byStatus.getOrDefault("PLANNED", 0L));
        System.out.println("Завершенных: " + byStatus.getOrDefault("COMPLETED", 0L));
        System.out.println("Отмененных: " + byStatus.getOrDefault("CANCELLED", 0L));
    }

    private void export() {
        Path path = exporter.export(memberService.findAll(), registrationService.findAll(),
            Path.of("exports", "sport-club-data.xlsx"));
        System.out.println("Экспорт завершен: " + path.toAbsolutePath());
    }

    private void printTables() {
        System.out.println("\n--- members ---");
        printMembers(memberService.findAll());
        System.out.println("--- training_registrations ---");
        printRegistrations(registrationService.findAll());
    }

    private void printMembers(List<Member> members) { members.forEach(System.out::println); }
    private void printRegistrations(List<TrainingRegistration> registrations) { registrations.forEach(System.out::println); }
    private String input(String label) { System.out.print(label); return scanner.nextLine().trim(); }
    private long inputLong(String label) { try { return Long.parseLong(input(label)); } catch (NumberFormatException exception) { throw new BusinessException("ID должен быть целым числом"); } }
    private int inputInt(String label) { try { return Integer.parseInt(input(label)); } catch (NumberFormatException exception) { throw new BusinessException("Значение должно быть целым числом"); } }
    private RegistrationStatus status() { return RegistrationStatus.valueOf(input("Статус (PLANNED/CONFIRMED/COMPLETED/CANCELLED): ").toUpperCase()); }
    private MembershipType membershipType() { return MembershipType.valueOf(input("Абонемент (BASIC/PREMIUM/ANNUAL): ").toUpperCase()); }
}
