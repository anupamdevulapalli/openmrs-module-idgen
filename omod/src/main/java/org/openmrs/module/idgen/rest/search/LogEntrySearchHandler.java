/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.idgen.rest.search;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import org.openmrs.module.idgen.IdentifierSource;
import org.openmrs.module.idgen.LogEntry;
import org.openmrs.module.idgen.service.IdentifierSourceService;
import org.openmrs.module.idgen.web.controller.IdgenRestController;
import org.openmrs.module.webservices.rest.web.ConversionUtil;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.resource.api.PageableResult;
import org.openmrs.module.webservices.rest.web.resource.api.SearchConfig;
import org.openmrs.module.webservices.rest.web.resource.api.SearchHandler;
import org.apache.commons.lang.StringEscapeUtils;
import org.openmrs.User;
import org.openmrs.api.UserService;
import org.openmrs.api.context.Context;
import org.openmrs.module.webservices.rest.web.resource.api.SearchQuery;
import org.openmrs.module.webservices.rest.web.resource.impl.EmptySearchResult;
import org.openmrs.module.webservices.rest.web.resource.impl.NeedsPaging;
import org.openmrs.module.webservices.rest.web.response.ResponseException;
import org.springframework.stereotype.Component;

@Component
public class LogEntrySearchHandler implements SearchHandler {

    private final SearchConfig searchConfig = new SearchConfig("default", RestConstants.VERSION_1 + IdgenRestController.IDGEN_NAMESPACE + "/logentry",
            Arrays.asList("1.8.*", "1.9.*", "1.10.*", "1.11.*", "1.12.*", "2.0.*", "2.1.*", "2.2.*", "2.3.*", "2.4.*"),
            Arrays.asList(new SearchQuery.Builder(
                    "Allows you to find log of ID Generation Activities by Source Name, Identifier contents,Generated Date Range,Comment contents and User who generated the log entry")
                            .withOptionalParameters("source", "identifier", "fromDate", "toDate", "comment",
                                    "generatedBy")
                            .build()));

    @Override
    public PageableResult search(RequestContext context) throws ResponseException {
        IdentifierSourceService identifierSourceService = Context.getService(IdentifierSourceService.class);
        UserService service = Context.getUserService();

        String source = sanitizeInput(context.getRequest().getParameter("source"));
        String fromDate = sanitizeInput(context.getRequest().getParameter("fromDate"));
        String toDate = sanitizeInput(context.getRequest().getParameter("toDate"));
        String identifier = sanitizeInput(context.getRequest().getParameter("identifier"));
        String comment = sanitizeInput(context.getRequest().getParameter("comment"));
        String generatedBy = sanitizeInput(context.getRequest().getParameter("generatedBy"));

        IdentifierSource logSource = source != null ? identifierSourceService.getIdentifierSourceByUuid(source) : null;
        Date dateFrom = fromDate != null ? (Date) ConversionUtil.convert(fromDate, Date.class) : null;
        Date dateTo = toDate != null ? (Date) ConversionUtil.convert(toDate, Date.class) : null;
        User user = generatedBy != null ? service.getUserByUuid(generatedBy): null;
        if (source != null && logSource == null) {
            return new EmptySearchResult();
        }
        else if (generatedBy != null && user == null) {
            return new EmptySearchResult();
        } 
        else {
            List<LogEntry> logEntries = identifierSourceService.getLogEntries(logSource, dateFrom, dateTo, identifier,
                    user, comment); 
            return new NeedsPaging<LogEntry>(logEntries, context);
        }
    }
    
	public String sanitizeInput(String in) {
 		return StringEscapeUtils.escapeHtml(in);
 	}
	
    @Override
    public SearchConfig getSearchConfig() {
        return searchConfig;
    }

}
