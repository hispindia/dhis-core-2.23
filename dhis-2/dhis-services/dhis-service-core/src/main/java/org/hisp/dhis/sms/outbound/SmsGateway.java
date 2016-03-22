package org.hisp.dhis.sms.outbound;

/*
 * Copyright (c) 2004-2016, University of Oslo
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * Neither the name of the HISP project nor the names of its contributors may
 * be used to endorse or promote products derived from this software without
 * specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

import java.util.List;

import org.hisp.dhis.sms.config.SmsGatewayConfig;
import org.springframework.http.HttpStatus;

import com.google.common.collect.ImmutableMap;

/**
 * Zubair <rajazubair.asghar@gmail.com>
 */

public interface SmsGateway
{
    ImmutableMap<HttpStatus, GatewayResponse> CLICKATELL_GATEWAY_RESPONSE_MAP = new ImmutableMap.Builder<HttpStatus, GatewayResponse>()
        .put( HttpStatus.OK, GatewayResponse.RESULT_CODE_200 )
        .put( HttpStatus.ACCEPTED, GatewayResponse.RESULT_CODE_202 )
        .put( HttpStatus.MULTI_STATUS, GatewayResponse.RESULT_CODE_207 )
        .put( HttpStatus.BAD_REQUEST, GatewayResponse.RESULT_CODE_400 )
        .put( HttpStatus.UNAUTHORIZED, GatewayResponse.RESULT_CODE_401 )
        .put( HttpStatus.PAYMENT_REQUIRED, GatewayResponse.RESULT_CODE_402 )
        .put( HttpStatus.NOT_FOUND, GatewayResponse.RESULT_CODE_404 )
        .put( HttpStatus.METHOD_NOT_ALLOWED, GatewayResponse.RESULT_CODE_405 )
        .put( HttpStatus.GONE, GatewayResponse.RESULT_CODE_410 )
        .put( HttpStatus.SERVICE_UNAVAILABLE, GatewayResponse.RESULT_CODE_503 ).build();

    ImmutableMap<String, GatewayResponse> BULKSMS_GATEWAY_RESPONSE_MAP = new ImmutableMap.Builder<String, GatewayResponse>()
        .put( "0", GatewayResponse.RESULT_CODE_0 ).put( "1", GatewayResponse.RESULT_CODE_1 )
        .put( "22", GatewayResponse.RESULT_CODE_22 ).put( "23", GatewayResponse.RESULT_CODE_23 )
        .put( "24", GatewayResponse.RESULT_CODE_24 ).put( "25", GatewayResponse.RESULT_CODE_25 )
        .put( "26", GatewayResponse.RESULT_CODE_26 ).put( "27", GatewayResponse.RESULT_CODE_27 )
        .put( "40", GatewayResponse.RESULT_CODE_40 ).build();

    GatewayResponse send( OutboundSms sms, SmsGatewayConfig gatewayConfig );

    GatewayResponse send( List<OutboundSms> sms, SmsGatewayConfig gatewayConfig );

    boolean accept( SmsGatewayConfig gatewayConfig );
}
