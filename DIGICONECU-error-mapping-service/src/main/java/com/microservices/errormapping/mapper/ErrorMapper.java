package com.microservices.errormapping.mapper;

import com.microservices.errormapping.dto.ErrorResponseDTO;
import com.microservices.errormapping.model.ErrorDefinition;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface ErrorMapper {

    ErrorMapper INSTANCE = Mappers.getMapper(ErrorMapper.class);

    ErrorResponseDTO toResponseDTO(ErrorDefinition errorDefinition);

    ErrorDefinition toModel(ErrorResponseDTO responseDTO);
}
