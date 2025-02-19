/*
 * Copyright 2016-2018 the original author or authors.
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
import org.springframework.samples.petclinic.mapper.SpecialtyMapper;
import org.springframework.samples.petclinic.mapper.VetMapper;
import org.springframework.samples.petclinic.model.Specialty;
import org.springframework.samples.petclinic.model.Vet;
import org.springframework.samples.petclinic.rest.api.VetsApi;
import org.springframework.samples.petclinic.rest.dto.VetDto;
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
@RequestMapping("api")
public class VetRestController implements VetsApi {

	private final ClinicService clinicService;
	private final VetMapper vetMapper;
	private final SpecialtyMapper specialtyMapper;

	public VetRestController(final ClinicService clinicService, final VetMapper vetMapper, final SpecialtyMapper specialtyMapper) {
		this.clinicService = clinicService;
		this.vetMapper = vetMapper;
		this.specialtyMapper = specialtyMapper;
	}

	@PreAuthorize("hasRole(@roles.VET_ADMIN)")
	@Override
	public ResponseEntity<List<VetDto>> listVets() {
		final List<VetDto> vets = new ArrayList<>();
		vets.addAll(vetMapper.toVetDtos(clinicService.findAllVets()));
		if (vets.isEmpty()) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		return new ResponseEntity<>(vets, HttpStatus.OK);
	}

	@PreAuthorize("hasRole(@roles.VET_ADMIN)")
	@Override
	public ResponseEntity<VetDto> getVet(final Integer vetId)  {
		final Vet vet = clinicService.findVetById(vetId);
		if (vet == null) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		return new ResponseEntity<>(vetMapper.toVetDto(vet), HttpStatus.OK);
	}

	@PreAuthorize("hasRole(@roles.VET_ADMIN)")
	@Override
	public ResponseEntity<VetDto> addVet(final VetDto vetDto) {
		final HttpHeaders headers = new HttpHeaders();
		final Vet vet = vetMapper.toVet(vetDto);
		clinicService.saveVet(vet);
		headers.setLocation(UriComponentsBuilder.newInstance().path("/api/vets/{id}").buildAndExpand(vet.getId()).toUri());
		return new ResponseEntity<>(vetMapper.toVetDto(vet), headers, HttpStatus.CREATED);
	}

	@PreAuthorize("hasRole(@roles.VET_ADMIN)")
	@Override
	public ResponseEntity<VetDto> updateVet(final Integer vetId,final VetDto vetDto)  {
		final Vet currentVet = clinicService.findVetById(vetId);
		if (currentVet == null) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		currentVet.setFirstName(vetDto.getFirstName());
		currentVet.setLastName(vetDto.getLastName());
		currentVet.clearSpecialties();
		for (final Specialty spec : specialtyMapper.toSpecialtys(vetDto.getSpecialties())) {
			currentVet.addSpecialty(spec);
		}
		clinicService.saveVet(currentVet);
		return new ResponseEntity<>(vetMapper.toVetDto(currentVet), HttpStatus.NO_CONTENT);
	}

	@PreAuthorize("hasRole(@roles.VET_ADMIN)")
	@Transactional
	@Override
	public ResponseEntity<VetDto> deleteVet(final Integer vetId) {
		final Vet vet = clinicService.findVetById(vetId);
		if (vet == null) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		clinicService.deleteVet(vet);
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}
}
