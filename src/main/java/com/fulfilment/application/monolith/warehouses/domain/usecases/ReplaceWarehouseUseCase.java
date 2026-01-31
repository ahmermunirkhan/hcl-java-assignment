package com.fulfilment.application.monolith.warehouses.domain.usecases;

import java.time.LocalDateTime;
import java.util.List;

import com.fulfilment.application.monolith.location.LocationGateway;
import com.fulfilment.application.monolith.warehouses.domain.models.Location;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.ReplaceWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.WebApplicationException;

@ApplicationScoped
public class ReplaceWarehouseUseCase implements ReplaceWarehouseOperation {

  private final WarehouseStore warehouseStore;
  private final LocationGateway locationResolver;

  public ReplaceWarehouseUseCase(WarehouseStore warehouseStore) {
    this.warehouseStore = warehouseStore;
    this.locationResolver = new LocationGateway();
  }

  @Override
  public void replace(Warehouse newWarehouse) {
    if(newWarehouse == null){
      throw new WebApplicationException("New warehouse cannot be null");
    }else{
      Warehouse existingWarehouse = warehouseStore.findByBusinessUnitCode(newWarehouse.businessUnitCode);
      if(existingWarehouse != null){
        Location location = locationResolver.resolveByIdentifier(newWarehouse.location);
        if(location == null){
          throw new WebApplicationException("Invalid warehouse location: " + newWarehouse.location);
        }else{
          if(warehouseStore.findByBusinessUnitCode(newWarehouse.businessUnitCode) != null){
            throw new WebApplicationException("Warehouse with businessUnitCode " + newWarehouse.businessUnitCode + " already exists");
          }
          List<Warehouse> warehousesAtLocation = warehouseStore.getAllByLocation(location.toString());
          if(warehousesAtLocation.size() - 1 >= location.maxNumberOfWarehouses){
            throw new WebApplicationException("Cannot create warehouse at location " + location.toString() + ". Maximum number of warehouses reached." );  
          }
          if(newWarehouse.capacity <= 0){
            throw new WebApplicationException("Warehouse capacity must be greater than zero");
          }else{
            int totalCapacityAtLocation = warehousesAtLocation.stream().mapToInt(w -> w.capacity).sum();
            if(totalCapacityAtLocation - existingWarehouse.capacity + newWarehouse.capacity > location.maxCapacity){
              throw new WebApplicationException("Cannot create warehouse at location " + location.toString() + ". Total capacity would exceed maximum allowed capacity of " + location.maxCapacity);
            }
          }
        }
        newWarehouse.stock = existingWarehouse.stock;
        newWarehouse.capacity = existingWarehouse.capacity;
        existingWarehouse.archivedAt = LocalDateTime.now();
        warehouseStore.update(existingWarehouse);
        warehouseStore.create(newWarehouse);
      }else{
        throw new WebApplicationException("Warehouse with businessUnitCode " + newWarehouse.businessUnitCode + " does not exist");
      }
    }

    warehouseStore.update(newWarehouse);
  }
}
