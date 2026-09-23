package ru.sportclub.repository;

import ru.sportclub.exception.DataAccessException;
import ru.sportclub.model.Member;
import ru.sportclub.model.MembershipType;
import ru.sportclub.util.DatabaseManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MemberRepository implements CrudRepository<Member> {
    private final DatabaseManager database;

    public MemberRepository(DatabaseManager database) {
        this.database = database;
    }

    @Override
    public Member save(Member member) {
        String sql = "INSERT INTO members(full_name, phone, email, membership_type, active) VALUES (?, ?, ?, ?, ?) RETURNING id";
        try (Connection connection = database.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, member.getFullName());
            statement.setString(2, member.getPhone());
            statement.setString(3, member.getEmail());
            statement.setString(4, member.getMembershipType().name());
            statement.setBoolean(5, member.isActive());
            try (ResultSet result = statement.executeQuery()) {
                result.next();
                member.setId(result.getLong(1));
                return member;
            }
        } catch (SQLException exception) {
            throw new DataAccessException("Не удалось сохранить участника", exception);
        }
    }

    @Override
    public List<Member> findAll() {
        String sql = "SELECT id, full_name, phone, email, membership_type, active FROM members ORDER BY id";
        List<Member> members = new ArrayList<>();
        try (Connection connection = database.getConnection(); PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {
            while (result.next()) {
                members.add(map(result));
            }
            return members;
        } catch (SQLException exception) {
            throw new DataAccessException("Не удалось получить участников", exception);
        }
    }

    @Override
    public Optional<Member> findById(long id) {
        String sql = "SELECT id, full_name, phone, email, membership_type, active FROM members WHERE id = ?";
        try (Connection connection = database.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            try (ResultSet result = statement.executeQuery()) {
                return result.next() ? Optional.of(map(result)) : Optional.empty();
            }
        } catch (SQLException exception) {
            throw new DataAccessException("Не удалось найти участника", exception);
        }
    }

    public Optional<Member> findByEmail(String email) {
        return findAll().stream().filter(member -> member.getEmail().equalsIgnoreCase(email)).findFirst();
    }

    @Override
    public Member update(Member member) {
        String sql = "UPDATE members SET full_name = ?, phone = ?, email = ?, membership_type = ?, active = ? WHERE id = ?";
        try (Connection connection = database.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, member.getFullName());
            statement.setString(2, member.getPhone());
            statement.setString(3, member.getEmail());
            statement.setString(4, member.getMembershipType().name());
            statement.setBoolean(5, member.isActive());
            statement.setLong(6, member.getId());
            statement.executeUpdate();
            return member;
        } catch (SQLException exception) {
            throw new DataAccessException("Не удалось изменить участника", exception);
        }
    }

    @Override
    public void deleteById(long id) {
        String sql = "DELETE FROM members WHERE id = ?";
        try (Connection connection = database.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            statement.executeUpdate();
        } catch (SQLException exception) {
            throw new DataAccessException("Не удалось удалить участника", exception);
        }
    }

    private Member map(ResultSet result) throws SQLException {
        return new Member(result.getLong("id"), result.getString("full_name"), result.getString("phone"),
                result.getString("email"), MembershipType.valueOf(result.getString("membership_type")),
                result.getBoolean("active"));
    }
}
