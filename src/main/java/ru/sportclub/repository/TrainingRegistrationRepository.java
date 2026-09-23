package ru.sportclub.repository;

import ru.sportclub.exception.DataAccessException;
import ru.sportclub.model.RegistrationStatus;
import ru.sportclub.model.TrainingRegistration;
import ru.sportclub.util.DatabaseManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TrainingRegistrationRepository implements CrudRepository<TrainingRegistration> {
    private final DatabaseManager database;

    public TrainingRegistrationRepository(DatabaseManager database) {
        this.database = database;
    }

    @Override
    public TrainingRegistration save(TrainingRegistration registration) {
        String sql = "INSERT INTO training_registrations(member_id, training_name, trainer_name, training_date, start_time, duration_minutes, status, category) VALUES (?, ?, ?, ?, ?, ?, ?, ?) RETURNING id, created_at";
        try (Connection connection = database.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            fill(statement, registration);
            try (ResultSet result = statement.executeQuery()) {
                result.next();
                registration.setId(result.getLong("id"));
                return registration;
            }
        } catch (SQLException exception) {
            throw new DataAccessException("Не удалось сохранить запись на тренировку", exception);
        }
    }

    @Override
    public List<TrainingRegistration> findAll() {
        return query("SELECT r.*, m.full_name AS member_name FROM training_registrations r JOIN members m ON m.id = r.member_id ORDER BY r.id");
    }

    @Override
    public Optional<TrainingRegistration> findById(long id) {
        String sql = "SELECT r.*, m.full_name AS member_name FROM training_registrations r JOIN members m ON m.id = r.member_id WHERE r.id = ?";
        try (Connection connection = database.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            try (ResultSet result = statement.executeQuery()) {
                return result.next() ? Optional.of(map(result)) : Optional.empty();
            }
        } catch (SQLException exception) {
            throw new DataAccessException("Не удалось найти запись на тренировку", exception);
        }
    }

    public boolean existsAtTime(long memberId, java.time.LocalDate date, java.time.LocalTime time, long ignoredId) {
        String sql = "SELECT EXISTS (SELECT 1 FROM training_registrations WHERE member_id = ? AND training_date = ? AND start_time = ? AND id <> ?)";
        try (Connection connection = database.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, memberId);
            statement.setDate(2, Date.valueOf(date));
            statement.setTime(3, Time.valueOf(time));
            statement.setLong(4, ignoredId);
            try (ResultSet result = statement.executeQuery()) {
                result.next();
                return result.getBoolean(1);
            }
        } catch (SQLException exception) {
            throw new DataAccessException("Не удалось проверить расписание участника", exception);
        }
    }

    @Override
    public TrainingRegistration update(TrainingRegistration registration) {
        String sql = "UPDATE training_registrations SET member_id = ?, training_name = ?, trainer_name = ?, training_date = ?, start_time = ?, duration_minutes = ?, status = ?, category = ? WHERE id = ?";
        try (Connection connection = database.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            fill(statement, registration);
            statement.setLong(9, registration.getId());
            statement.executeUpdate();
            return registration;
        } catch (SQLException exception) {
            throw new DataAccessException("Не удалось изменить запись на тренировку", exception);
        }
    }

    @Override
    public void deleteById(long id) {
        String sql = "DELETE FROM training_registrations WHERE id = ?";
        try (Connection connection = database.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            statement.executeUpdate();
        } catch (SQLException exception) {
            throw new DataAccessException("Не удалось удалить запись на тренировку", exception);
        }
    }

    private List<TrainingRegistration> query(String sql) {
        List<TrainingRegistration> registrations = new ArrayList<>();
        try (Connection connection = database.getConnection(); PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {
            while (result.next()) {
                registrations.add(map(result));
            }
            return registrations;
        } catch (SQLException exception) {
            throw new DataAccessException("Не удалось получить записи на тренировки", exception);
        }
    }

    private void fill(PreparedStatement statement, TrainingRegistration registration) throws SQLException {
        statement.setLong(1, registration.getMemberId());
        statement.setString(2, registration.getTrainingName());
        statement.setString(3, registration.getTrainerName());
        statement.setDate(4, Date.valueOf(registration.getTrainingDate()));
        statement.setTime(5, Time.valueOf(registration.getStartTime()));
        statement.setInt(6, registration.getDurationMinutes());
        statement.setString(7, registration.getStatus().name());
        statement.setString(8, registration.getCategory());
    }

    private TrainingRegistration map(ResultSet result) throws SQLException {
        return new TrainingRegistration(result.getLong("id"), result.getLong("member_id"), result.getString("member_name"),
                result.getString("training_name"), result.getString("trainer_name"), result.getDate("training_date").toLocalDate(),
                result.getTime("start_time").toLocalTime(), result.getInt("duration_minutes"),
                RegistrationStatus.valueOf(result.getString("status")), result.getString("category"),
                result.getTimestamp("created_at").toLocalDateTime());
    }
}
