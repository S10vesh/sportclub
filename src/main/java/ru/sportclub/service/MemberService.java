package ru.sportclub.service;

import ru.sportclub.exception.BusinessException;
import ru.sportclub.exception.EntityNotFoundException;
import ru.sportclub.model.Member;
import ru.sportclub.repository.CrudRepository;

import java.util.List;

public class MemberService {
    private final CrudRepository<Member> repository;

    public MemberService(CrudRepository<Member> repository) {
        this.repository = repository;
    }

    public Member create(Member member) {
        validate(member);
        return repository.save(member);
    }

    public List<Member> findAll() { return repository.findAll(); }

    public Member findById(long id) {
        return repository.findById(id).orElseThrow(() -> new EntityNotFoundException("Участник не найден: " + id));
    }

    public Member update(Member member) {
        findById(member.getId());
        validate(member);
        return repository.update(member);
    }

    public void delete(long id) {
        findById(id);
        repository.deleteById(id);
    }

    private void validate(Member member) {
        if (member.getFullName() == null || member.getFullName().isBlank()) {
            throw new BusinessException("ФИО участника обязательно");
        }
        if (member.getPhone() == null || member.getPhone().isBlank()) {
            throw new BusinessException("Телефон участника обязателен");
        }
        if (member.getEmail() == null || !member.getEmail().contains("@")) {
            throw new BusinessException("Email участника некорректен");
        }
    }
}
