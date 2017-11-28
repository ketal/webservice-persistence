/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.ketal.webservice.controller;

import java.util.List;

import com.github.ketal.persistence.jpa.controller.JpaController;
import com.github.ketal.webservice.exception.NonExistingEntityException;
import com.github.ketal.webservice.exception.PreExistingEntityException;

public abstract class AbstractWsController<T, E, C> implements WsController<T> {

    protected C config;
    
    protected AbstractWsController(C config) {
        this.config = config;
    }
    
    /*
     * return instance of JpaController
     */
    abstract protected JpaController<E> getJpaController();

    /*
     * Finds and returns entity based on given DO object.
     * 
     *  Find entity based on unique properties.
     *  For example: 
     *      jpaController.findBy(Persona_.username, personaDO.getUsername());
     */
    abstract protected List<E> findPreExistingEntity(T object);

    /*
     * Convert given Entity object to DO object.
     *  - convertRelationships: if true, convert and add  OneToMany and/or ManyToMany relationships
     */
    abstract protected T convertEntity(E object, boolean convertRelationships);

    /*
     * Convert given DO object to Entity object.
     */
    abstract protected E convertDO(T object);

    /*
     * Convert given DO object to given Entity object
     * returns the passed in entity parameter after the conversion 
     */
    abstract protected E convertDO(T object, E entity);

    @Override
    public int post(T object) throws Exception {
        List<E> entities = findPreExistingEntity(object);

        if (entities != null && !entities.isEmpty()) {
            throw new PreExistingEntityException("Entity already exists.");
        }

        E entity = convertDO(object);
        getJpaController().create(entity);
        return (int) getJpaController().getPrimaryKey(entity);
    }

    @Override
    public T get(int id) throws Exception {
        E entity = getJpaController().find(id);
        if (entity == null) {
            throw new NonExistingEntityException("Could not find Entity with id '" + id + "'");
        }

        T object = convertEntity(entity, true);
        return object;
    }

    @Override
    public void put(int id, T object) throws Exception {
        E entity = getJpaController().find(id);
        if (entity == null) {
            throw new NonExistingEntityException("Could not find Entity with id '" + id + "'");
        }

        entity = convertDO(object, entity);
        getJpaController().update(entity);
    }

    @Override
    public void delete(int id) throws Exception {
        E entity = getJpaController().find(id);
        if (entity == null) {
            throw new NonExistingEntityException("Could not find Entity with id '" + id + "'");
        }

        getJpaController().delete(entity);
    }

}