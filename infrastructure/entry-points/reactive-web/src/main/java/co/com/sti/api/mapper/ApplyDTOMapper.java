package co.com.sti.api.mapper;

import co.com.sti.api.dto.ApplyDTO;
import co.com.sti.model.apply.Apply;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ApplyDTOMapper {

    ApplyDTO toResponse(Apply apply);
    Apply toModel(ApplyDTO applyDTO);

}
