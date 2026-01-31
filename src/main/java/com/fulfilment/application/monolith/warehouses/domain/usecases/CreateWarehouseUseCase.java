package com.fulfilment.application.monolith.warehouses.domain.usecases;

import java.util.List;

import com.fulfilment.application.monolith.location.LocationGateway;
import com.fulfilment.application.monolith.warehouses.domain.models.Location;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.CreateWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.WebApplicationException;

@ApplicationScoped
public class CreateWarehouseUseCase implements CreateWarehouseOperation {

  private final WarehouseStore warehouseStore;
  private LocationGateway locationResolver;

  public CreateWarehouseUseCase(WarehouseStore warehouseStore) {
    this.warehouseStore = warehouseStore;
    this.locationResolver = new LocationGateway();
  }

  @Override
  public void create(Warehouse warehouse) {
    if(warehouse == null){
      throw new WebApplicationException("Warehouse cannot be null");
    }
    Location location = locationResolver.resolveByIdentifier(warehouse.location);
    if(location == null){
      throw new WebApplicationException("Invalid warehouse location: " + warehouse.location);
    }else{
      if(warehouseStore.findByBusinessUnitCode(warehouse.businessUnitCode) != null){
        throw new WebApplicationException("Warehouse with businessUnitCode " + warehouse.businessUnitCode + " already exists");
      }
      List<Warehouse> warehousesAtLocation = warehouseStore.getAllByLocation(location.toString());
      if(warehousesAtLocation.size() >= location.maxNumberOfWarehouses){
        throw new WebApplicationException("Cannot create warehouse at location " + location.toString() + ". Maximum number of warehouses reached." );  
      }
      if(warehouse.capacity <= 0){
        throw new WebApplicationException("Warehouse capacity must be greater than zero");
      }else{
        int totalCapacityAtLocation = warehousesAtLocation.stream().mapToInt(w -> w.capacity).sum();
        if(totalCapacityAtLocation + warehouse.capacity > location.maxCapacity){
          throw new WebApplicationException("Cannot create warehouse at location " + location.toString() + ". Total capacity would exceed maximum allowed capacity of " + location.maxCapacity);
        }
      }
    }
    warehouse.location = location.toString();
    warehouseStore.create(warehouse);
  }
}
