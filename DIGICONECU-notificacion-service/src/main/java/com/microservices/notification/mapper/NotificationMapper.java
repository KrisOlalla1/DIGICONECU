package com.microservices.notification.mapper;

import com.microservices.notification.dto.NotificationRequestDTO;
import com.microservices.notification.model.NotificationRequest;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface NotificationMapper {

    NotificationMapper INSTANCE = Mappers.getMapper(NotificationMapper.class);

    NotificationRequest toModel(NotificationRequestDTO dto);

    NotificationRequestDTO toDTO(NotificationRequest model);
}
