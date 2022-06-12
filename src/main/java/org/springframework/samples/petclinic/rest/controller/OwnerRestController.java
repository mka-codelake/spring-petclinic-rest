/*
 * Copyright 2016-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.samples.petclinic.rest.controller;

import java.util.Collection;
import java.util.List;

import javax.transaction.Transactional;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.samples.petclinic.mapper.OwnerMapper;
import org.springframework.samples.petclinic.mapper.PetMapper;
import org.springframework.samples.petclinic.mapper.VisitMapper;
import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.model.Pet;
import org.springframework.samples.petclinic.model.Visit;
import org.springframework.samples.petclinic.rest.api.OwnersApi;
import org.springframework.samples.petclinic.rest.dto.OwnerDto;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.dto.PetDto;
import org.springframework.samples.petclinic.rest.dto.PetFieldsDto;
import org.springframework.samples.petclinic.rest.dto.VisitDto;
import org.springframework.samples.petclinic.rest.dto.VisitFieldsDto;
import org.springframework.samples.petclinic.service.ClinicService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * @author Vitaliy Fedoriv
 */

@RestController
@CrossOrigin(exposedHeaders = "errors, content-type")
@RequestMapping("/api")
public class OwnerRestController implements OwnersApi {

	private final ClinicService clinicService;

	private final OwnerMapper ownerMapper;

	private final PetMapper petMapper;

	private final VisitMapper visitMapper;

	public OwnerRestController(final ClinicService clinicService,
			final OwnerMapper ownerMapper,
			final PetMapper petMapper,
			final VisitMapper visitMapper) {
		this.clinicService = clinicService;
		this.ownerMapper = ownerMapper;
		this.petMapper = petMapper;
		this.visitMapper = visitMapper;
	}

	@PreAuthorize("hasRole(@roles.OWNER_ADMIN)")
	@Override
	public ResponseEntity<List<OwnerDto>> listOwners(final String lastName) {
		Collection<Owner> owners;
		if (lastName != null) {
			owners = clinicService.findOwnerByLastName(lastName);
		} else {
			owners = clinicService.findAllOwners();
		}
		if (owners.isEmpty()) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		return new ResponseEntity<>(ownerMapper.toOwnerDtoCollection(owners), HttpStatus.OK);
	}

	@PreAuthorize("hasRole(@roles.OWNER_ADMIN)")
	@Override
	public ResponseEntity<OwnerDto> getOwner(final Integer ownerId) {
		final Owner owner = clinicService.findOwnerById(ownerId);
		if (owner == null) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		return new ResponseEntity<>(ownerMapper.toOwnerDto(owner), HttpStatus.OK);
	}

	@PreAuthorize("hasRole(@roles.OWNER_ADMIN)")
	@Override
	public ResponseEntity<OwnerDto> addOwner(final OwnerFieldsDto ownerFieldsDto) {
		final HttpHeaders headers = new HttpHeaders();
		final Owner owner = ownerMapper.toOwner(ownerFieldsDto);
		clinicService.saveOwner(owner);
		final OwnerDto ownerDto = ownerMapper.toOwnerDto(owner);
		headers.setLocation(UriComponentsBuilder.newInstance()
				.path("/api/owners/{id}").buildAndExpand(owner.getId()).toUri());
		return new ResponseEntity<>(ownerDto, headers, HttpStatus.CREATED);
	}

	@PreAuthorize("hasRole(@roles.OWNER_ADMIN)")
	@Override
	public ResponseEntity<OwnerDto> updateOwner(final Integer ownerId, final OwnerFieldsDto ownerFieldsDto) {
		final Owner currentOwner = clinicService.findOwnerById(ownerId);
		if (currentOwner == null) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		currentOwner.setAddress(ownerFieldsDto.getAddress());
		currentOwner.setCity(ownerFieldsDto.getCity());
		currentOwner.setFirstName(ownerFieldsDto.getFirstName());
		currentOwner.setLastName(ownerFieldsDto.getLastName());
		currentOwner.setTelephone(ownerFieldsDto.getTelephone());
		clinicService.saveOwner(currentOwner);
		return new ResponseEntity<>(ownerMapper.toOwnerDto(currentOwner), HttpStatus.NO_CONTENT);
	}

	@PreAuthorize("hasRole(@roles.OWNER_ADMIN)")
	@Transactional
	@Override
	public ResponseEntity<OwnerDto> deleteOwner(final Integer ownerId) {
		final Owner owner = clinicService.findOwnerById(ownerId);
		if (owner == null) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		clinicService.deleteOwner(owner);
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}

	@PreAuthorize("hasRole(@roles.OWNER_ADMIN)")
	@Override
	public ResponseEntity<PetDto> addPetToOwner(final Integer ownerId, final PetFieldsDto petFieldsDto) {
		final HttpHeaders headers = new HttpHeaders();
		final Pet pet = petMapper.toPet(petFieldsDto);
		final Owner owner = new Owner();
		owner.setId(ownerId);
		pet.setOwner(owner);
		clinicService.savePet(pet);
		final PetDto petDto = petMapper.toPetDto(pet);
		headers.setLocation(UriComponentsBuilder.newInstance().path("/api/pets/{id}")
				.buildAndExpand(pet.getId()).toUri());
		return new ResponseEntity<>(petDto, headers, HttpStatus.CREATED);
	}

	@PreAuthorize("hasRole(@roles.OWNER_ADMIN)")
	@Override
	public ResponseEntity<VisitDto> addVisitToOwner(final Integer ownerId, final Integer petId, final VisitFieldsDto visitFieldsDto) {
		final HttpHeaders headers = new HttpHeaders();
		final Visit visit = visitMapper.toVisit(visitFieldsDto);
		final Pet pet = new Pet();
		pet.setId(petId);
		visit.setPet(pet);
		clinicService.saveVisit(visit);
		final VisitDto visitDto = visitMapper.toVisitDto(visit);
		headers.setLocation(UriComponentsBuilder.newInstance().path("/api/visits/{id}")
				.buildAndExpand(visit.getId()).toUri());
		return new ResponseEntity<>(visitDto, headers, HttpStatus.CREATED);
	}

}
