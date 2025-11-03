package com.asdc.unicarpool.mapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class Mapper {

    private final ModelMapper modelMapper;

    @Autowired
    public Mapper(ObjectMapper objectMapper, ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
    }

    public <T> T map(Object source, Class<T> destinationClass) {
        return modelMapper.map(source, destinationClass);
    }

    public <T> List<T> mapList(List<?> source, Class<T> destinationClass) {
        return source.stream()
                .map(element -> modelMapper.map(element, destinationClass))
                .toList();
    }
}
