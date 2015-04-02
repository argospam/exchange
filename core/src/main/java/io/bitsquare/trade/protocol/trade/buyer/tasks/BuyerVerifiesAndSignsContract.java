/*
 * This file is part of Bitsquare.
 *
 * Bitsquare is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at
 * your option) any later version.
 *
 * Bitsquare is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with Bitsquare. If not, see <http://www.gnu.org/licenses/>.
 */

package io.bitsquare.trade.protocol.trade.buyer.tasks;

import io.bitsquare.common.taskrunner.TaskRunner;
import io.bitsquare.trade.Contract;
import io.bitsquare.trade.Trade;
import io.bitsquare.trade.protocol.trade.TradeTask;
import io.bitsquare.util.Utilities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BuyerVerifiesAndSignsContract extends TradeTask {
    private static final Logger log = LoggerFactory.getLogger(BuyerVerifiesAndSignsContract.class);

    public BuyerVerifiesAndSignsContract(TaskRunner taskHandler, Trade trade) {
        super(taskHandler, trade);
    }

    @Override
    protected void doRun() {
        try {
            Contract contract = new Contract(
                    processModel.getOffer(),
                    trade.getTradeAmount(),
                    processModel.getTakeOfferFeeTxId(),
                    processModel.getAccountId(),
                    processModel.tradingPeer.getAccountId(),
                    processModel.getFiatAccount(),
                    processModel.tradingPeer.getFiatAccount(),
                    processModel.getP2pSigPubKey(),
                    processModel.tradingPeer.getP2pSigPubKey());
            String contractAsJson = Utilities.objectToJson(contract);
            String signature = processModel.getSignatureService().signMessage(processModel.getRegistrationKeyPair(), contractAsJson);
            trade.setContract(contract);
            trade.setContractAsJson(contractAsJson);
            trade.setBuyerContractSignature(signature);
            trade.setBuyerContractSignature(processModel.tradingPeer.getContractSignature());

            complete();
        } catch (Throwable t) {
            t.printStackTrace();
            trade.setThrowable(t);

            StateUtil.setOfferOpenState(trade);

            failed(t);
        }
    }
}