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

import java.util.ArrayList;
import java.util.List;

import javax.transaction.Transactional;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.samples.petclinic.mapper.PetTypeMapper;
import org.springframework.samples.petclinic.model.PetType;
import org.springframework.samples.petclinic.rest.api.PettypesApi;
import org.springframework.samples.petclinic.rest.dto.PetTypeDto;
import org.springframework.samples.petclinic.service.ClinicService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import io.swagger.annotations.Api;

@RestController
@CrossOrigin(exposedHeaders = "errors, content-type")
@Api(tags = { "pettypes" })
@RequestMapping("api")
public class PetTypeRestController implements PettypesApi {

	private final ClinicService clinicService;
	private final PetTypeMapper petTypeMapper;


	public PetTypeRestController(final ClinicService clinicService, final PetTypeMapper petTypeMapper) {
		this.clinicService = clinicService;
		this.petTypeMapper = petTypeMapper;
	}

	@PreAuthorize("hasAnyRole(@roles.OWNER_ADMIN, @roles.VET_ADMIN)")
	@Override
	public ResponseEntity<List<PetTypeDto>> listPetTypes() {
		final List<PetType> petTypes = new ArrayList<>(clinicService.findAllPetTypes());
		if (petTypes.isEmpty()) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		return new ResponseEntity<>(petTypeMapper.toPetTypeDtos(petTypes), HttpStatus.OK);
	}

	@PreAuthorize("hasAnyRole(@roles.OWNER_ADMIN, @roles.VET_ADMIN)")
	@Override
	public ResponseEntity<PetTypeDto> getPetType(final Integer petTypeId) {
		final PetType petType = clinicService.findPetTypeById(petTypeId);
		if (petType == null) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		return new ResponseEntity<>(petTypeMapper.toPetTypeDto(petType), HttpStatus.OK);
	}

	@PreAuthorize("hasRole(@roles.VET_ADMIN)")
	@Override
	public ResponseEntity<PetTypeDto> addPetType(final PetTypeDto petTypeDto) {
		final HttpHeaders headers = new HttpHeaders();
		final PetType type = petTypeMapper.toPetType(petTypeDto);
		clinicService.savePetType(type);
		headers.setLocation(UriComponentsBuilder.newInstance().path("/api/pettypes/{id}").buildAndExpand(type.getId()).toUri());
		return new ResponseEntity<>(petTypeMapper.toPetTypeDto(type), headers, HttpStatus.CREATED);
	}

	@PreAuthorize("hasRole(@roles.VET_ADMIN)")
	@Override
	public ResponseEntity<PetTypeDto> updatePetType(final Integer petTypeId, final PetTypeDto petTypeDto) {
		final PetType currentPetType = clinicService.findPetTypeById(petTypeId);
		if (currentPetType == null) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		currentPetType.setName(petTypeDto.getName());
		clinicService.savePetType(currentPetType);
		return new ResponseEntity<>(petTypeMapper.toPetTypeDto(currentPetType), HttpStatus.NO_CONTENT);
	}

	@PreAuthorize("hasRole(@roles.VET_ADMIN)")
	@Transactional
	@Override
	public ResponseEntity<PetTypeDto> deletePetType(final Integer petTypeId) {
		final PetType petType = clinicService.findPetTypeById(petTypeId);
		if (petType == null) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		clinicService.deletePetType(petType);
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}

}
