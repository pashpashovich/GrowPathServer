package by.bsuir.growpathserver.trainee.infrastructure.converter;

import by.bsuir.growpathserver.trainee.domain.valueobject.InternshipProgramStatus;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class InternshipProgramStatusConverter implements AttributeConverter<InternshipProgramStatus, String> {

    @Override
    public String convertToDatabaseColumn(InternshipProgramStatus status) {
        if (status == null) {
            return null;
        }
        return status.getValue();
    }

    @Override
    public InternshipProgramStatus convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return null;
        }
        return InternshipProgramStatus.fromString(dbData);
    }
}
