package Agents;

import java.util.Arrays;
import java.util.List;

import jade.core.Agent;

public class Client extends Agent {
	
	public List<String> product = Arrays.asList("cotton", "chiffon", "voile");
	public List<String> quantity = Arrays.asList("small", "medium", "large");
	public List<String> quality = Arrays.asList("low", "medium", "high");
	public List<String> delivery = Arrays.asList("short", "standard", "long");
	
}
