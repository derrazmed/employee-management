package com.employeemanagement.service;

import com.employeemanagement.entity.Employee;
import com.employeemanagement.entity.Permission;
import com.employeemanagement.exception.ResourceNotFoundException;
import com.employeemanagement.repository.EmployeeRepository;
import com.employeemanagement.security.PermissionService;
import lombok.RequiredArgsConstructor;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class ContractService {

    private final EmployeeRepository employeeRepository;
    private final PermissionService permissionService;

    public byte[] generateContract(Long employeeId) {

        permissionService.requirePermission(Permission.READ);

        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Employee not found with id: " + employeeId
                        )
                );

        try {
            return createPdf(employee);
        } catch (IOException exception) {
            throw new RuntimeException(
                    "Could not generate employee contract",
                    exception
            );
        }
    }

    private byte[] createPdf(Employee employee) throws IOException {

        try (
                PDDocument document = new PDDocument();
                ByteArrayOutputStream outputStream =
                        new ByteArrayOutputStream()
        ) {

            PDPage page = new PDPage(PDRectangle.A4);

            document.addPage(page);

            try (PDPageContentStream content =
                         new PDPageContentStream(document, page)) {

                float margin = 60;
                float y = 770;

                // =========================
                // TITLE
                // =========================

                content.beginText();

                content.setFont(
                        new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD),
                        18
                );

                content.newLineAtOffset(190, y);

                content.showText("EMPLOYMENT CONTRACT");

                content.endText();

                // =========================
                // CONTRACT DATE
                // =========================

                y -= 50;

                content.beginText();

                content.setFont(
                        new PDType1Font(Standard14Fonts.FontName.HELVETICA),
                        11
                );

                content.newLineAtOffset(margin, y);

                content.showText(
                        "Contract date: " +
                                LocalDate.now().format(
                                        DateTimeFormatter.ofPattern("dd/MM/yyyy")
                                )
                );

                content.endText();

                // =========================
                // INTRODUCTION
                // =========================

                y -= 50;

                y = writeLine(
                        content,
                        "This employment contract is established between",
                        margin,
                        y
                );

                y -= 20;

                y = writeLine(
                        content,
                        "the company and the following employee:",
                        margin,
                        y
                );

                // =========================
                // EMPLOYEE INFORMATION
                // =========================

                y -= 35;

                y = writeField(
                        content,
                        "Employee ID",
                        String.valueOf(employee.getId()),
                        margin,
                        y
                );

                y = writeField(
                        content,
                        "First Name",
                        employee.getFirstName(),
                        margin,
                        y
                );

                y = writeField(
                        content,
                        "Last Name",
                        employee.getLastName(),
                        margin,
                        y
                );

                y = writeField(
                        content,
                        "Email",
                        employee.getEmail(),
                        margin,
                        y
                );

                y = writeField(
                        content,
                        "Phone",
                        valueOrNotSpecified(employee.getPhoneNumber()),
                        margin,
                        y
                );

                y = writeField(
                        content,
                        "Job Title",
                        employee.getJobTitle(),
                        margin,
                        y
                );

                y = writeField(
                        content,
                        "Department",
                        valueOrNotSpecified(employee.getDepartment()),
                        margin,
                        y
                );

                y = writeField(
                        content,
                        "Hire Date",
                        employee.getHireDate()
                                .format(
                                        DateTimeFormatter.ofPattern(
                                                "dd/MM/yyyy"
                                        )
                                ),
                        margin,
                        y
                );

                y = writeField(
                        content,
                        "Salary",
                        formatSalary(employee.getSalary()),
                        margin,
                        y
                );

                // =========================
                // CONTRACT TERMS
                // =========================

                y -= 35;

                content.beginText();

                content.setFont(
                        new PDType1Font(
                                Standard14Fonts.FontName.HELVETICA_BOLD
                        ),
                        13
                );

                content.newLineAtOffset(margin, y);

                content.showText("Contract Terms");

                content.endText();

                y -= 25;

                y = writeLine(
                        content,
                        "The employee agrees to perform the duties",
                        margin,
                        y
                );

                y = writeLine(
                        content,
                        "associated with the position of "
                                + employee.getJobTitle() + ".",
                        margin,
                        y
                );

                y -= 15;

                y = writeLine(
                        content,
                        "The employment start date is "
                                + employee.getHireDate()
                                .format(
                                        DateTimeFormatter.ofPattern(
                                                "dd/MM/yyyy"
                                        )
                                ) + ".",
                        margin,
                        y
                );

                // =========================
                // SIGNATURES
                // =========================

                y -= 60;

                content.beginText();

                content.setFont(
                        new PDType1Font(
                                Standard14Fonts.FontName.HELVETICA
                        ),
                        11
                );

                content.newLineAtOffset(margin, y);

                content.showText("Employer Signature:");

                content.newLineAtOffset(280, 0);

                content.showText("Employee Signature:");

                content.endText();

                y -= 50;

                content.beginText();

                content.newLineAtOffset(margin, y);

                content.showText("________________________");

                content.newLineAtOffset(280, 0);

                content.showText("________________________");

                content.endText();
            }

            document.save(outputStream);

            return outputStream.toByteArray();
        }
    }

    private float writeLine(
            PDPageContentStream content,
            String text,
            float x,
            float y
    ) throws IOException {

        content.beginText();

        content.setFont(
                new PDType1Font(Standard14Fonts.FontName.HELVETICA),
                11
        );

        content.newLineAtOffset(x, y);

        content.showText(text);

        content.endText();

        return y - 18;
    }

    private float writeField(
            PDPageContentStream content,
            String label,
            String value,
            float x,
            float y
    ) throws IOException {

        content.beginText();

        content.setFont(
                new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD),
                11
        );

        content.newLineAtOffset(x, y);

        content.showText(label + ": ");

        content.setFont(
                new PDType1Font(Standard14Fonts.FontName.HELVETICA),
                11
        );

        content.showText(value);

        content.endText();

        return y - 22;
    }

    private String valueOrNotSpecified(String value) {

        if (value == null || value.isBlank()) {
            return "Not specified";
        }

        return value;
    }

    private String formatSalary(BigDecimal salary) {

        if (salary == null) {
            return "Not specified";
        }

        return salary + " MAD";
    }
}