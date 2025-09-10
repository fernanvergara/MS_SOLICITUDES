package co.com.sti.api.mapper;

import co.com.sti.api.dto.RequestDTO;
import co.com.sti.model.request.Request;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface RequestDTOMapper {

    RequestDTO toDTO(Request request);
    Request toModel(RequestDTO requestDTO);
}
