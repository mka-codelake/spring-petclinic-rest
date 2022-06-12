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
import org.springframework.samples.petclinic.mapper.VisitMapper;
import org.springframework.samples.petclinic.model.Visit;
import org.springframework.samples.petclinic.rest.api.VisitsApi;
import org.springframework.samples.petclinic.rest.dto.VisitDto;
import org.springframework.samples.petclinic.service.ClinicService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import io.swagger.annotations.Api;

/**
 * @author Vitaliy Fedoriv
 */

@RestController
@CrossOrigin(exposedHeaders = "errors, content-type")
@Api(tags = { "visit" })
@RequestMapping("api")
public class VisitRestController implements VisitsApi {

	private final ClinicService clinicService;

	private final VisitMapper visitMapper;

	public VisitRestController(final ClinicService clinicService, final VisitMapper visitMapper) {
		this.clinicService = clinicService;
		this.visitMapper = visitMapper;
	}


	@PreAuthorize("hasRole(@roles.OWNER_ADMIN)")
	@Override
	public ResponseEntity<List<VisitDto>> listVisits() {
		final List<Visit> visits = new ArrayList<>(clinicService.findAllVisits());
		if (visits.isEmpty()) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		return new ResponseEntity<>(new ArrayList<>(visitMapper.toVisitsDto(visits)), HttpStatus.OK);
	}

	@PreAuthorize("hasRole(@roles.OWNER_ADMIN)")
	@Override
	public ResponseEntity<VisitDto> getVisit( final Integer visitId) {
		final Visit visit = clinicService.findVisitById(visitId);
		if (visit == null) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		return new ResponseEntity<>(visitMapper.toVisitDto(visit), HttpStatus.OK);
	}

	@PreAuthorize("hasRole(@roles.OWNER_ADMIN)")
	@Override
	public ResponseEntity<VisitDto> addVisit(VisitDto visitDto) {
		final HttpHeaders headers = new HttpHeaders();
		final Visit visit = visitMapper.toVisit(visitDto);
		clinicService.saveVisit(visit);
		visitDto = visitMapper.toVisitDto(visit);
		headers.setLocation(UriComponentsBuilder.newInstance().path("/api/visits/{id}").buildAndExpand(visit.getId()).toUri());
		return new ResponseEntity<>(visitDto, headers, HttpStatus.CREATED);
	}

	@PreAuthorize("hasRole(@roles.OWNER_ADMIN)")
	@Override
	public ResponseEntity<VisitDto> updateVisit(final Integer visitId, final VisitDto visitDto) {
		final Visit currentVisit = clinicService.findVisitById(visitId);
		if (currentVisit == null) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		currentVisit.setDate(visitDto.getDate());
		currentVisit.setDescription(visitDto.getDescription());
		clinicService.saveVisit(currentVisit);
		return new ResponseEntity<>(visitMapper.toVisitDto(currentVisit), HttpStatus.NO_CONTENT);
	}

	@PreAuthorize("hasRole(@roles.OWNER_ADMIN)")
	@Transactional
	@Override
	public ResponseEntity<VisitDto> deleteVisit(final Integer visitId) {
		final Visit visit = clinicService.findVisitById(visitId);
		if (visit == null) {
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		clinicService.deleteVisit(visit);
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}

}
