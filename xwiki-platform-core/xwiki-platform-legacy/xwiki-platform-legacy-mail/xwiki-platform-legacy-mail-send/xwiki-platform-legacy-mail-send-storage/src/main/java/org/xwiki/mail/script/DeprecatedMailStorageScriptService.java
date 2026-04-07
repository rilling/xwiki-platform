/*
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.xwiki.mail.script;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.commons.lang3.tuple.Pair;
import org.xwiki.component.annotation.Component;
import org.xwiki.mail.MailResender;
import org.xwiki.mail.MailStatus;
import org.xwiki.mail.MailStatusResult;
import org.xwiki.mail.MailStoreException;
import org.xwiki.mail.internal.DefaultMailResult;

/**
 * Expose Mail Storage API to scripts.
 * <p>
 * This deprecated service delegates to {@link MailStorageScriptService} for all shared functionality,
 * only overriding what differs (error key and legacy resend behavior).
 *
 * @version $Id$
 * @since 6.4M3
 * @deprecated since 12.4RC1, use {@link MailStorageScriptService} instead
 */
@Component
@Named("mailstorage")
@Singleton
@Deprecated
public class DeprecatedMailStorageScriptService extends MailStorageScriptService
{
    /**
     * The key under which the last encountered error is stored in the current execution context.
     */
    private static final String ERROR_KEY = "scriptservice.mailstorage.error";

    @Inject
    @Named("database")
    private MailResender mailResender;

    /**
     * Resends all mails matching the passed filter map.
     *
     * @param filterMap the map of Mail Status parameters to match (e.g. "state", "wiki", "batchId", etc)
     * @param offset the number of rows to skip (0 means don't skip any row)
     * @param count the number of rows to return. If 0 then all rows are returned
     * @return the mail results for the resent mails and null if an error occurred while loading the mail statuses
     *         from the store
     * @since 9.3RC1
     */
    @Override
    public List<ScriptMailResult> resendAsynchronously(Map<String, Object> filterMap, int offset, int count)
    {
        List<Pair<MailStatus, MailStatusResult>> results;
        try {
            results = this.mailResender.resendAsynchronously(filterMap, offset, count);
        } catch (MailStoreException e) {
            // Save the exception for reporting through the script services's getLastError() API
            setError(e);
            return null;
        }

        List<ScriptMailResult> scriptResults = new ArrayList<>();
        for (Pair<MailStatus, MailStatusResult> result : results) {
            scriptResults.add(new ScriptMailResult(
                new DefaultMailResult(result.getLeft().getBatchId()), result.getRight()));
        }
        return scriptResults;
    }

    @Override
    protected String getErrorKey()
    {
        return ERROR_KEY;
    }
}
