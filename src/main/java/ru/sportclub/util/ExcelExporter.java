package ru.sportclub.util;

import ru.sportclub.model.TrainingRegistration;
import ru.sportclub.model.Member;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class ExcelExporter {
    public Path export(List<Member> members, List<TrainingRegistration> registrations, Path output) {
        try {
            Files.createDirectories(output.getParent());
            try (Workbook workbook = new XSSFWorkbook()) {
                Sheet membersSheet = workbook.createSheet("Участники");
                String[] memberHeaders = {"ID", "ФИО", "Телефон", "Email", "Абонемент", "Активен"};
                Row memberHeader = membersSheet.createRow(0);
                for (int index = 0; index < memberHeaders.length; index++) {
                    memberHeader.createCell(index).setCellValue(memberHeaders[index]);
                }
                for (int rowIndex = 0; rowIndex < members.size(); rowIndex++) {
                    Member member = members.get(rowIndex);
                    Row row = membersSheet.createRow(rowIndex + 1);
                    row.createCell(0).setCellValue(member.getId());
                    row.createCell(1).setCellValue(member.getFullName());
                    row.createCell(2).setCellValue(member.getPhone());
                    row.createCell(3).setCellValue(member.getEmail());
                    row.createCell(4).setCellValue(member.getMembershipType().name());
                    row.createCell(5).setCellValue(member.isActive());
                }
                for (int index = 0; index < memberHeaders.length; index++) {
                    membersSheet.autoSizeColumn(index);
                }

                Sheet sheet = workbook.createSheet("Записи на тренировки");
                String[] headers = {"ID", "Участник", "Тренировка", "Тренер", "Дата", "Время", "Минуты", "Статус", "Категория"};
                Row header = sheet.createRow(0);
                for (int index = 0; index < headers.length; index++) {
                    header.createCell(index).setCellValue(headers[index]);
                }
                for (int rowIndex = 0; rowIndex < registrations.size(); rowIndex++) {
                    TrainingRegistration item = registrations.get(rowIndex);
                    Row row = sheet.createRow(rowIndex + 1);
                    row.createCell(0).setCellValue(item.getId());
                    row.createCell(1).setCellValue(item.getMemberName());
                    row.createCell(2).setCellValue(item.getTrainingName());
                    row.createCell(3).setCellValue(item.getTrainerName());
                    row.createCell(4).setCellValue(item.getTrainingDate().toString());
                    row.createCell(5).setCellValue(item.getStartTime().toString());
                    row.createCell(6).setCellValue(item.getDurationMinutes());
                    row.createCell(7).setCellValue(item.getStatus().name());
                    row.createCell(8).setCellValue(item.getCategory());
                }
                for (int index = 0; index < headers.length; index++) {
                    sheet.autoSizeColumn(index);
                }
                try (var stream = Files.newOutputStream(output)) {
                    workbook.write(stream);
                }
            }
            return output;
        } catch (IOException exception) {
            throw new IllegalStateException("Не удалось экспортировать данные", exception);
        }
    }
}
