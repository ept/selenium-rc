/*
 * Copyright (c) 2003-2005, Wiley & Sons, Joe Walnes,Ara Abrahamian,
 * Mike Cannon-Brookes,Patrick A Lightbody
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer
 * in the documentation and/or other materials provided with the distribution.
 *     * Neither the name of the 'Wiley & Sons', 'Java Open Source
 * Programming' nor the names of the authors may be used to endorse or
 * promote products derived from this software without specific prior
 * written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.petsoar.pets;

import org.petsoar.categories.Category;

import java.util.List;

/**
 * Entry point to the PetStore.
 */
public interface PetStore {

    /**
     * Add a pet to the store.
     */
    void savePet(Pet pet);

    /**
     * Remove a pet from the store.
     */
    void removePet(Pet pet);

    /**
     * List all pets in the store.
     */
    List getPets();

    /**
     * List all pets that have not been categorized.
     */
    List getUncategorizedPets();

    /**
     * Get a pet by id.
     */
    Pet getPet(long id);

    /**
     * Add a category to the store. The category is considered to be a root category if it doesn't have a parent
     * category. All sub-categories of this new category are also added to the store automatically.
     */
    void addCategory(Category category);

    /**
     * Remove a category from the store. All the pets of this category and all subcategories are also removed from the
     * store automatically.
     */
    void removeCategory(Category category);

    /**
     * List all the root categories in the store.
     */
    List getRootCategories();

    /**
     * Get a category by id.
     */
    Category getCategory(long id);

}
