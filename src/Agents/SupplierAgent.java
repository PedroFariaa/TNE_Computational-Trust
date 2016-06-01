package Agents;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import jade.core.Agent;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.ContractNetResponder;

public class SupplierAgent extends Agent {

	Double w = Math.PI / 18;
	Double lambda_F = 1.0;
	Double lambda_Fd = -0.1;
	Double lambda_V = -1.5;
	int dec = 0;
	String res = "";

	List<String> product = Arrays.asList("cotton", "chiffon", "voile");
	List<String> quantity = Arrays.asList("small", "medium", "large");
	List<String> quality = Arrays.asList("low", "medium", "high");
	List<String> delivery = Arrays.asList("short", "standard", "long");

	ArrayList<String> handicap = new ArrayList<>();
	List<List<String>> handicap_string = Arrays.asList(product, quantity, quality, delivery);

	/**
	 * Setup method. Starts by generating the supplier handicaps and then adds a
	 * FIPAContractNetResp behaviour.
	 */
	public void setup() {

		generateSupplierHandicaps();
		addBehaviour(new FIPAContractNetResp(this, MessageTemplate.MatchPerformative(ACLMessage.CFP)));

	}

	/**
	 * Generates the supplier handicaps (randomized type and respective
	 * parameter).
	 */
	private void generateSupplierHandicaps() {

		Random rand = new Random();
		Integer handicap_number = rand.nextInt(2);

		for (Integer i = 0; i < handicap_number; i++) {

			Integer handicap_type = rand.nextInt(4);
			Integer handicap_param = rand.nextInt(3);

			handicap.add(handicap_string.get(handicap_type).get(handicap_param));

		}

	}

	class FIPAContractNetResp extends ContractNetResponder {

		Double trust = 0.0;
		boolean hasHandicap = false;

		/**
		 * Class constructor with two parameters.
		 * 
		 * @param a
		 *            - Agent.
		 * @param mt
		 *            - Message Template.
		 */
		public FIPAContractNetResp(Agent a, MessageTemplate mt) {

			super(a, mt);

		}

		/**
		 * Checks if the message contains any handicap of the supplier. Assumed
		 * Message: <product>, <quantity>, <quality>, <delivery>
		 * 
		 * @param msg
		 *            - ACLMessage to be checked.
		 * @return Returns true if the message contains any handicap of the
		 *         supplier, and false otherwise.
		 */
		protected boolean checkHandicap(ACLMessage msg) {

			String[] msg_params = msg.getContent().split(", ");

			for (String param : msg_params)
				if (handicap.contains(param))
					return true;

			return false;

		}

		/**
		 * Handle the call for proposal and return the reply.
		 * @param cfp - Call for proposal
		 * @return Returns the reply to the sender of the call for proposal.
		 */
		protected ACLMessage handleCfp(ACLMessage cfp) {
			
			hasHandicap = checkHandicap(cfp);
			
			if(hasHandicap)
				System.out.println("[" + myAgent.getLocalName() + "]: " + "Found handicap from " + cfp.getSender().getLocalName());
			
			ACLMessage reply = cfp.createReply();
			reply.setPerformative(ACLMessage.PROPOSE);
			reply.setContent("" + trust);

			System.out.println("[" + myAgent.getLocalName() + "]: " + "Sending trust to " + cfp.getSender().getLocalName());
			//System.out.println("" + trust);
			return reply;
			
		}

		/**
		 * Handles the reject proposal.
		 * 
		 * @param cfp
		 *            - Call for proposal.
		 * @param propose
		 *            - Propose message.
		 * @param reject
		 *            - Reject message.
		 */
		protected void handleRejectProposal(ACLMessage cfp, ACLMessage propose, ACLMessage reject) {

			System.out.println(
					"[" + myAgent.getLocalName() + "]: Received a reject from " + cfp.getSender().getLocalName());

		}

		/**
		 * Returns the reply to the accept proposal.
		 * 
		 * @param cfp
		 *            - Call for proposal.
		 * @param propose
		 *            - Proposal message.
		 * @param accept
		 *            - Accept message.
		 */
		protected ACLMessage handleAcceptProposal(ACLMessage cfp, ACLMessage propose, ACLMessage accept) {

			//System.out.println(myAgent.getLocalName() + " got an accept!");

			// decidir envio F, Fd ou V
			setResult();

			// atualiza valor de trust - SINALPHA
			//System.out.println(">>>>>>>>>>> " + dec);
			double old_trust = trust;
			if (dec == 0)
				trust = trust + lambda_F * w;
			if (dec == 1)
				trust = trust + lambda_Fd * w;
			if (dec == 2)
				trust = trust + lambda_V * w;
			
			if(trust > 1)
				trust = (double) 1;
			if(trust < 0)
				trust = (double) 0;
			System.out.println("[" + myAgent.getLocalName() + "]: " + "Updated trust from " + old_trust + " to " + trust);
			// envia mensagem para cliente - acho que nao vai ser necessario
			ACLMessage result = accept.createReply();
			result.setPerformative(ACLMessage.INFORM);
			result.setContent(res);
			return result;

		}
		
		private void setResult() {
			// F - 0; Fd - 1; V - 2
			Random rand = new Random();
			Integer r = rand.nextInt(101);
			int val = r / 100;

			if (!hasHandicap) {
				if (val <= trust * (2 / 3) + 0.2){
					dec = 0;
					res = "Fulfilled";
				}
				else if (val <= ((trust * (2 / 3) + 0.2) + (1 - (trust * (2 / 3) + 0.2)) * 0.6)){
					dec = 1;
					res = "Fulfilled with delay";
				}
				else{
					dec = 2;
					res = "Not fulfilled";
				}
			}else{
				if (val <= trust * (2 / 3) - 0.1){
					dec = 0;
					res = "Fulfilled";
				}
				else if (val <= ((trust * (2 / 3) - 0.1) + (1 - (trust * (2 / 3) - 0.1)) * 0.7)){
					dec = 1;
					res = "Fulfilled with delay";
				}
				else{
					dec = 2;
					res = "Not fulfilled";
				}
			}
		}

	}
}
