/*
 * Copyright 2018-2019 adorsys GmbH & Co KG
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.adorsys.psd2.xs2a.web.link;

import de.adorsys.psd2.xs2a.domain.Links;

public class TransactionsReportDownloadLinks extends AbstractLinks {

    public TransactionsReportDownloadLinks(String httpUrl, String accountId, boolean withBalance, String downloadId, Links links) {
        super(httpUrl);

        if (links != null) {
            setFirst(links.getFirst());
            setNext(links.getNext());
            setPrevious(links.getPrevious());
            setLast(links.getLast());
        }

        if (downloadId != null) {
            setDownload(buildPath(UrlHolder.ACCOUNT_TRANSACTIONS_DOWNLOAD_URL, accountId, downloadId));
        }

        if (withBalance) {
            setBalances(buildPath(UrlHolder.ACCOUNT_BALANCES_URL, accountId));
        }
    }
}
