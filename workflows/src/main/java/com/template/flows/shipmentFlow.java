package com.template.flows;

import co.paralleluniverse.fibers.Suspendable;
import com.template.contracts.CarContract;
import com.template.states.CarState;
import net.corda.core.flows.*;
import net.corda.core.identity.Party;
import net.corda.core.transactions.SignedTransaction;
import net.corda.core.transactions.TransactionBuilder;
import net.corda.core.utilities.ProgressTracker;
import static com.template.contracts.CarContract.CAR_CONTRACT_ID;


@InitiatingFlow
    @StartableByRPC
    public  class shipmentFlow extends FlowLogic<SignedTransaction>{
        private String model;
        private Party owner ;

        private final ProgressTracker progressTracker = new ProgressTracker();

        public shipmentFlow(String model, Party owner) {
            this.model = model;
            this.owner = owner;
        }


        @Override
        @Suspendable
        public SignedTransaction call() throws FlowException {


            // Initiator flow logic goes here

            if (getOurIdentity().getName().getOrganisation().equals("Tesla")){
                System.out.println("Identity verified");
            } else {
                throw new FlowException("Identity not verified");
            }

            //Retrieve the notary identity from the network map4

            Party notary= getServiceHub().getNetworkMapCache().getNotaryIdentities().get(0);

            //Create the transaction Components(Inputs/Outputs)

            CarState outputState = new CarState(model,owner ,getOurIdentity());


            //Create the transaction builder and add components

            TransactionBuilder txBuilder = new TransactionBuilder(notary)
                   .addOutputState(outputState,CAR_CONTRACT_ID)
                   .addCommand(new CarContract.shipment(), getOurIdentity().getOwningKey());

            //txBuilder.setNotary(notary);
            //txBuilder.addOutputState(outputState,CAR_CONTRACT_ID);

            //Signing the transaction

            SignedTransaction shipmentTx = getServiceHub().signInitialTransaction(txBuilder);

            //create session with counterparty
            FlowSession otherPartySession = initiateFlow(owner);

            //Finalizing the transaction
            return subFlow(new FinalityFlow(shipmentTx , otherPartySession));
        }
    }

