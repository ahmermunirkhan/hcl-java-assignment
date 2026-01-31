package com.fulfilment.application.monolith.warehouses.adapters.restapi;

import com.fulfilment.application.monolith.warehouses.adapters.database.WarehouseRepository;
import com.fulfilment.application.monolith.warehouses.domain.ports.ArchiveWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.ReplaceWarehouseOperation;
import com.warehouse.api.WarehouseResource;
import com.warehouse.api.beans.Warehouse;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.WebApplicationException;

import java.util.List;

@RequestScoped
public class WarehouseResourceImpl implements WarehouseResource{

  @Inject private WarehouseRepository warehouseRepository;

  @Inject private ReplaceWarehouseOperation replaceWarehouseOperation;

  @Inject private ArchiveWarehouseOperation archiveWarehouseOperation;

  @Override
  public List<Warehouse> listAllWarehousesUnits() {
    return warehouseRepository.getAll().stream().map(this::toWarehouseResponse).toList();
  }

  @Override
  public Warehouse createANewWarehouseUnit(@NotNull Warehouse data) {
    if(data == null){
      throw new WebApplicationException("Warehouse data cannot be null");
    }else{
      com.fulfilment.application.monolith.warehouses.domain.models.Warehouse warehouse = new com.fulfilment.application.monolith.warehouses.domain.models.Warehouse();
      warehouse.businessUnitCode = data.getBusinessUnitCode();
      warehouse.location = data.getLocation();
      warehouse.capacity = data.getCapacity();
      warehouse.stock = data.getStock();
      warehouseRepository.create(warehouse);
      return data;
    }
  }

  @Override
  public Warehouse getAWarehouseUnitByID(String id) {
    com.fulfilment.application.monolith.warehouses.domain.models.Warehouse warehouse = warehouseRepository.findByBusinessUnitCode(id);
    if(warehouse == null){
      throw new WebApplicationException("Warehouse with businessUnitCode " + id + " does not exist");
    }else{
      return toWarehouseResponse(warehouse);
    } 
  }

  @Override
  public void archiveAWarehouseUnitByID(String id) {
    com.fulfilment.application.monolith.warehouses.domain.models.Warehouse warehouse = warehouseRepository.findByBusinessUnitCode(id);
    if(warehouse == null){
      throw new WebApplicationException("Warehouse with businessUnitCode " + id + " does not exist");
    }else{
      warehouse.archivedAt = java.time.LocalDateTime.now();
      warehouseRepository.update(warehouse);
    }
  }

  @Override
  public Warehouse replaceTheCurrentActiveWarehouse(
      String businessUnitCode, @NotNull Warehouse data) {
    if(data == null){
      throw new WebApplicationException("Warehouse data cannot be null");
    }else{
      if(businessUnitCode.equals(data.getBusinessUnitCode())){
        throw new WebApplicationException("BusinessUnitCode in path and body are same");
      }else{
        com.fulfilment.application.monolith.warehouses.domain.models.Warehouse existingWarehouse = warehouseRepository.findByBusinessUnitCode(businessUnitCode);
        if(existingWarehouse == null){
          throw new WebApplicationException("Warehouse with businessUnitCode " + businessUnitCode + " does not exist");
        }
        archiveWarehouseOperation.archive(existingWarehouse);
      }
      com.fulfilment.application.monolith.warehouses.domain.models.Warehouse warehouse = new com.fulfilment.application.monolith.warehouses.domain.models.Warehouse();
      warehouse.businessUnitCode = businessUnitCode;
      warehouse.location = data.getLocation();
      warehouse.capacity = data.getCapacity();
      warehouse.stock = data.getStock();
      replaceWarehouseOperation.replace(warehouse);
      return data;
    }
  } 
  private Warehouse toWarehouseResponse(
      com.fulfilment.application.monolith.warehouses.domain.models.Warehouse warehouse) {
    var response = new Warehouse();
    response.setBusinessUnitCode(warehouse.businessUnitCode);
    response.setLocation(warehouse.location);
    response.setCapacity(warehouse.capacity);
    response.setStock(warehouse.stock);

    return response;
  }
}
