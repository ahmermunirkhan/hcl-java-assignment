package com.fulfilment.application.monolith.warehouses.adapters.database;

import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.WebApplicationException;

import java.util.List;

@ApplicationScoped
public class WarehouseRepository implements WarehouseStore, PanacheRepository<DbWarehouse> {

  @Override
  public List<Warehouse> getAll() {
    return this.listAll().stream().map(DbWarehouse::toWarehouse).toList();
  }

  public List<Warehouse> getAllByLocation(String location) {
    return this.listAll().stream().filter(dbWarehouse -> dbWarehouse.location.equals(location)).map(DbWarehouse::toWarehouse).toList();
  }

  @Override
  public void create(Warehouse warehouse) {
    if(warehouse != null){
      if(find("businessUnitCode = ?1 AND archivedAt IS NULL", warehouse.businessUnitCode).firstResult() != null){
        throw new WebApplicationException("Warehouse with businessUnitCode " + warehouse.businessUnitCode + " already exists");
      }
      if(warehouse.location == null || warehouse.location.isBlank()){
        throw new WebApplicationException("Warehouse location cannot be null or empty");
      }
      persist(new DbWarehouse(warehouse));
      return;
    }
    throw new WebApplicationException("Warehouse cannot be null");
  }

  @Override
  public void update(Warehouse warehouse) {
    if(warehouse == null) {
      throw new WebApplicationException("Warehouse cannot be null");
    }
    DbWarehouse dbWarehouse = find("businessUnitCode = ?1 AND archivedAt IS NULL", warehouse.businessUnitCode).firstResult();
    if(dbWarehouse == null) {
      throw new WebApplicationException("Warehouse with businessUnitCode " + warehouse.businessUnitCode + " does not exist");
    }
    if(warehouse.location == null || warehouse.location.isBlank()){
        throw new WebApplicationException("Warehouse location cannot be null or empty");
    }
    dbWarehouse.location = warehouse.location;
    dbWarehouse.capacity = warehouse.capacity;
    dbWarehouse.stock = warehouse.stock;
    persist(dbWarehouse);
  } 

  @Override
  public void remove(Warehouse warehouse) {
    if (warehouse !=null) {
      DbWarehouse dbWarehouse = find("businessUnitCode = ?1 AND archivedAt IS NULL", warehouse.businessUnitCode).firstResult();
      if (dbWarehouse != null) {
        delete(dbWarehouse);
        return;
      } else {
        throw new WebApplicationException("Warehouse with businessUnitCode " + warehouse.businessUnitCode + " does not exist");
      }      
    }else {
      throw new WebApplicationException("Warehouse cannot be null");
    }
  }

  @Override
  public Warehouse findByBusinessUnitCode(String buCode) {
    DbWarehouse dbWarehouse = find("businessUnitCode = ?1 AND archivedAt IS NULL", buCode).firstResult();
    return dbWarehouse != null ? dbWarehouse.toWarehouse() : null;
  }
}
