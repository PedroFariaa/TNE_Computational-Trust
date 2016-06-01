package Main;

import java.util.ArrayList;
import java.util.List;

import Agents.BenevolentClientAgent;
import Agents.SupplierAgent;
import Agents.ClientAgent;
import Utils.Constants;
import jade.BootProfileImpl;
import jade.core.ProfileImpl;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;
import jade.core.Runtime;

public class main {

	static boolean JADE_GUI = true;
	private static ProfileImpl profile;
	private static ContainerController mainContainer;
	static Constants c = new Constants();
	
	public static void main(String[] args) throws StaleProxyException {

		// Init JADE platform w/ or w/out GUI
		if (JADE_GUI) {
			List<String> params = new ArrayList<String>();
			params.add("-gui");
			profile = new BootProfileImpl(params.toArray(new String[0]));
		}

		Runtime rt = Runtime.instance();

		// mainContainer - agents' container
		ContainerController mainContainer = rt.createMainContainer(profile);
		System.out.println("container created");

	/*	AgentController rma = mainContainer.createNewAgent("rma", "jade.tools.rma.rma", null);
		rma.start();
		*/
		// creating agents
		Object[] agentArgs = new Object[0];
		for (int i = 0; i < c.getSup_number(); i++) {
			AgentController supplier = mainContainer.createNewAgent("sup" + i, "Agents.SupplierAgent",
					agentArgs);
			supplier.start();
		}

		Object[] agentArgs2 = new Object[0];
		for (int i = 0; i < c.getClients_number(); i++) {
			AgentController client = mainContainer.createNewAgent("client" + i, "Agents.ClientAgent", agentArgs2);
			client.start();
		}		

	}

	public static ContainerController getContainer() {
		return mainContainer;
	}

	public static void setContainer(ContainerController mainContainer) {
		main.mainContainer = mainContainer;
	}

}
