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
import org.springframework.samples.petclinic.mapper.SpecialtyMapper;
import org.springframework.samples.petclinic.model.Specialty;
import org.springframework.samples.petclinic.rest.api.SpecialtiesApi;
import org.springframework.samples.petclinic.rest.dto.SpecialtyDto;
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
public class SpecialtyRestController implements SpecialtiesApi {

	private final ClinicService clinicService;

	private final SpecialtyMapper specialtyMapper;

	public SpecialtyRestController(final ClinicService clinicService, final SpecialtyMapper specialtyMapper) {
		this.clinicService = clinicService;
		this.specialtyMapper = specialtyMapper;
	}

	@PreAuthorize("hasRole(@roles.VET_ADMIN)")
	@Override
	public ResponseEntity<List<SpecialtyDto>> listSpecialties() {
		final List<SpecialtyDto> specialties = new ArrayList<>();
		specialties.addAll(specialtyMapper.toSpecialtyDtos(clinicService.findAllSpecialties()));
		if (specialties.isEmpty()) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		return new ResponseEntity<>(specialties, HttpStatus.OK);
	}

	@PreAuthorize("hasRole(@roles.VET_ADMIN)")
	@Override
	public ResponseEntity<SpecialtyDto> getSpecialty(final Integer specialtyId) {
		final Specialty specialty = clinicService.findSpecialtyById(specialtyId);
		if (specialty == null) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		return new ResponseEntity<>(specialtyMapper.toSpecialtyDto(specialty), HttpStatus.OK);
	}

	@PreAuthorize("hasRole(@roles.VET_ADMIN)")
	@Override
	public ResponseEntity<SpecialtyDto> addSpecialty(final SpecialtyDto specialtyDto) {
		final HttpHeaders headers = new HttpHeaders();
		final Specialty specialty = specialtyMapper.toSpecialty(specialtyDto);
		clinicService.saveSpecialty(specialty);
		headers.setLocation(UriComponentsBuilder.newInstance().path("/api/specialtys/{id}").buildAndExpand(specialty.getId()).toUri());
		return new ResponseEntity<>(specialtyMapper.toSpecialtyDto(specialty), headers, HttpStatus.CREATED);
	}

	@PreAuthorize("hasRole(@roles.VET_ADMIN)")
	@Override
	public ResponseEntity<SpecialtyDto> updateSpecialty(final Integer specialtyId, final SpecialtyDto specialtyDto) {
		final Specialty currentSpecialty = clinicService.findSpecialtyById(specialtyId);
		if (currentSpecialty == null) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		currentSpecialty.setName(specialtyDto.getName());
		clinicService.saveSpecialty(currentSpecialty);
		return new ResponseEntity<>(specialtyMapper.toSpecialtyDto(currentSpecialty), HttpStatus.NO_CONTENT);
	}

	@PreAuthorize("hasRole(@roles.VET_ADMIN)")
	@Transactional
	@Override
	public ResponseEntity<SpecialtyDto> deleteSpecialty(final Integer specialtyId) {
		final Specialty specialty = clinicService.findSpecialtyById(specialtyId);
		if (specialty == null) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		clinicService.deleteSpecialty(specialty);
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}

}
