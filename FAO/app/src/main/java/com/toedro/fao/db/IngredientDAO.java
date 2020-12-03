package com.toedro.fao.db;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public abstract class IngredientDAO {
    @Query("SELECT * FROM Ingredients WHERE id = :id")
    public abstract Ingredient getIngredient(String id);
    @Query("SELECT * FROM Ingredients WHERE id IN (:id)")
    public abstract List<Ingredient> getIngredients(List<String> id);
    @Query("SELECT * FROM Ingredients")
    public abstract List<Ingredient> getIngredients();
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public abstract void addIngredients(List<Ingredient> ingredients);
}