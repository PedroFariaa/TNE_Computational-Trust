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
	
	Double w = Math.PI/6;
	Double lambda_F = 1.0;
	Double lambda_Fd = -0.1;
	Double lambda_V = -1.5;
	
	List<String> product = Arrays.asList("cotton", "chiffon", "voile");
	List<String> quantity = Arrays.asList("small", "medium", "large");
	List<String> quality = Arrays.asList("low", "medium", "high");
	List<String> delivery = Arrays.asList("short", "standard", "long");
	
	ArrayList<String> handicap = new ArrayList<>();
	List<List<String>> handicap_string = Arrays.asList(product, quantity, quality, delivery);
	
	
	public void setup() {
		generateSupplierHandicaps();	
		addBehaviour(new FIPAContractNetResp(this, MessageTemplate.MatchPerformative(ACLMessage.CFP)));
	}
	
	private void generateSupplierHandicaps() {
		Random rand = new Random();
		Integer handicap_number = rand.nextInt(2);
		
		for(Integer i = 0; i < handicap_number; i++){
			Integer handicap_type = rand.nextInt(4);
			Integer handicap_param = rand.nextInt(3);
			
			handicap.add(handicap_string.get(handicap_type).get(handicap_param));
		}
	}
	
	class FIPAContractNetResp extends ContractNetResponder {
		
		Double trust = 0.0;

		public FIPAContractNetResp(Agent a, MessageTemplate mt) {
			super(a, mt);
		}
		
		
		/**
		 * Checks if the message contains any handicap of the supplier.
		 * Assumed Message: <product>, <quantity>, <quality>, <delivery>
		 * @param msg - ACLMessage to be checked.
		 * @return Returns true if the message contains any handicap of the supplier, and false otherwise.
		 */
		protected boolean checkHandicap(ACLMessage msg) {
			
			String [] msg_params = msg.getContent().split(", ");
			
			for(String param : msg_params)
				if(handicap.contains(param))
					return true;
				
			return false;
			
		}
		
		protected ACLMessage handleCfp(ACLMessage cfp) {
			ACLMessage reply = cfp.createReply();
			reply.setPerformative(ACLMessage.PROPOSE);
			reply.setContent("" + trust);
			// ...
			System.out.println("sent trust to agent");
			return reply;
		}
		
		protected void handleRejectProposal(ACLMessage cfp, ACLMessage propose, ACLMessage reject) {
			
			System.out.println("[" + myAgent.getLocalName() + "]: Received a reject from " + cfp.getSender().getLocalName());
			
		}

		protected ACLMessage handleAcceptProposal(ACLMessage cfp, ACLMessage propose, ACLMessage accept) {
			System.out.println(myAgent.getLocalName() + " got an accept!");
			
			//decidir envio F, Fd ou V
			
			
			//atualiza valor de trust - SINALPHA
			trust = trust + lambda_F * w;
			
			
			//envia mensagem para cliente - acho que nao vai ser necessario
			ACLMessage result = accept.createReply();
			result.setPerformative(ACLMessage.INFORM);
			result.setContent("this is the result");
			
			return result;
		}

	}
}
