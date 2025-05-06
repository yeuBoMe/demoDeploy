package com.computer.demoComputer.domain.dto;

import java.util.List;
import java.util.Optional;


public class ProductCriteriaDTO {
    private Optional<String> pageOptional;
    private Optional<String> nameOptional;
    private Optional<String> sortOptional;
    private Optional<List<String>> factoryOptional;
    private Optional<List<String>> priceOptional;
    private Optional<List<String>> targetOptional;

    public Optional<String> getPageOptional() {
        return pageOptional;
    }

    public void setPageOptional(Optional<String> pageOptional) {
        this.pageOptional = pageOptional;
    }

    public Optional<String> getNameOptional() {
        return nameOptional;
    }

    public void setNameOptional(Optional<String> nameOptional) {
        this.nameOptional = nameOptional;
    }

    public Optional<String> getSortOptional() {
        return sortOptional;
    }

    public void setSortOptional(Optional<String> sortOptional) {
        this.sortOptional = sortOptional;
    }

    public Optional<List<String>> getFactoryOptional() {
        return factoryOptional;
    }

    public void setFactoryOptional(Optional<List<String>> factoryOptional) {
        this.factoryOptional = factoryOptional;
    }

    public Optional<List<String>> getPriceOptional() {
        return priceOptional;
    }

    public void setPriceOptional(Optional<List<String>> priceOptional) {
        this.priceOptional = priceOptional;
    }

    public Optional<List<String>> getTargetOptional() {
        return targetOptional;
    }

    public void setTargetOptional(Optional<List<String>> targetOptional) {
        this.targetOptional = targetOptional;
    }

}
