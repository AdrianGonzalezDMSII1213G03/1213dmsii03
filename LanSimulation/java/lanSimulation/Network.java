/*   This file is part of lanSimulation.
 *
 *   lanSimulation is free software; you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation; either version 2 of the License, or
 *   (at your option) any later version.
 *
 *   lanSimulation is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with lanSimulation; if not, write to the Free Software
 *   Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 *
 *   Copyright original Java version: 2004 Bart Du Bois, Serge Demeyer
 *   Copyright C++ version: 2006 Matthias Rieger, Bart Van Rompaey
 */
 package lanSimulation;

import lanSimulation.internals.*;
import java.util.Hashtable;
import java.util.Enumeration;
import java.io.*;

/**
A <em>Network</em> represents the basic data stucture for simulating a Local Area Network (LAN).
The LAN network architecture is a token ring, implying that packahes will be passed from one node to another, until they reached their destination, or until they travelled the whole token ring.
*/
public class Network {
    /**
    Holds a pointer to myself.
    Used to verify whether I am properly initialized.
    */
    private Network initPtr_;
    /**
    Holds a pointer to some "first" node in the token ring.
    Used to ensure that various printing operations return expected behaviour.
    */
    public Node firstNode_;
    /**
    Maps the names of workstations on the actual workstations.
    Used to initiate the requests for the network.
    */
    public Hashtable workstations_;

/**
Construct a <em>Network</em> suitable for holding #size Workstations.
<p><strong>Postcondition:</strong>(result.isInitialized()) & (! result.consistentNetwork());</p>
*/
    public Network(int size) {
        assert size > 0;
	initPtr_ = this;
	firstNode_ = null;
        workstations_ = new Hashtable(size, 1.0f);
        assert isInitialized();
        assert ! consistentNetwork();
    }

/**
Return a <em>Network</em> that may serve as starting point for various experiments.
Currently, the network looks as follows.
    <pre>
    Workstation Filip [Workstation] -> Node -> Workstation Hans [Workstation]
    -> Printer Andy [Printer] -> ... 
    </pre>
<p><strong>Postcondition:</strong>result.isInitialized() & result.consistentNetwork();</p>
*/
    public static Network DefaultExample () {
        Network network = new Network (2);

        Node wsFilip = new Node (Node.WORKSTATION, "Filip");
    	Node n1 = new Node(Node.NODE, "n1");
        Node wsHans = new Node (Node.WORKSTATION, "Hans");
        Node prAndy = new Node (Node.PRINTER, "Andy");
        
        wsFilip.nextNode_ = n1;
        n1.nextNode_ = wsHans;
        wsHans.nextNode_ = prAndy;
        prAndy.nextNode_ = wsFilip;
        
        network.workstations_.put(wsFilip.name_, wsFilip);
        network.workstations_.put(wsHans.name_, wsHans);
        network.firstNode_ = wsFilip;

        assert network.isInitialized();
        assert network.consistentNetwork();
        return network;
    }

/**
Answer whether #receiver is properly initialized.
*/
    public boolean isInitialized () {
	return (initPtr_ == this);
    };

/**
Answer whether #receiver contains a workstation with the given name.
<p><strong>Precondition:</strong>this.isInitialized();</p>
*/
    public boolean hasWorkstation (String ws) {
	//return workstations_.containsKey(ws);
	Node n;

        assert isInitialized();
	n = (Node) workstations_.get(ws);
	if (n == null) {
	    return false;
	} else {
	    return n.type_ == Node.WORKSTATION;
	}
    };
        
/**
Answer whether #receiver is a consistent token ring network.
A consistent token ring network
 - contains at least one workstation and one printer
 - is circular
 - all registered workstations are on the token ring
 - all workstations on the token ring are registered.
<p><strong>Precondition:</strong>this.isInitialized();</p>
*/
    public boolean consistentNetwork () {
        assert isInitialized();
	Enumeration iter;
	Node currentNode;
	int printersFound = 0, workstationsFound = 0;
	Hashtable encountered = new Hashtable(workstations_.size() * 2, 1.0f);
	
	if (workstations_.isEmpty()) {return false;};
	if (firstNode_ == null) {return false;};
	//verify whether all registered workstations are indeed workstations
	iter = workstations_.elements();
	while (iter.hasMoreElements()) {
	    currentNode = (Node) iter.nextElement();
	    if (currentNode.type_ != Node.WORKSTATION) {return false;};
	};
	//enumerate the token ring, verifying whether all workstations are registered
	//also count the number of printers and see whether the ring is circular
	currentNode = firstNode_;
	while (! encountered.containsKey(currentNode.name_)) {
	    encountered.put(currentNode.name_, currentNode);
	    if (currentNode.type_ == Node.WORKSTATION) {workstationsFound++;};
	    if (currentNode.type_ == Node.PRINTER) {printersFound++;};
	    currentNode = currentNode.nextNode_;
	};
	if (currentNode != firstNode_) {return false;};//not circular
	if (printersFound == 0) {return false;};//does not contain a printer
	if (workstationsFound != workstations_.size()) {return false;}; //not all workstations are registered
	//all verifications succeedeed
        return true;}

public void printAccounting(Writer report, String author, String title)
			throws IOException {
		report.write("\tAccounting -- author = '");
		report.write(author);
		report.write("' -- title = '");
		report.write(title);
		report.write("'\n");
	}
    
/**
Return a printable representation of #receiver.
 <p><strong>Precondition:</strong> isInitialized();</p>
 */
    public String toString () {
        assert isInitialized();
	StringBuffer buf = new StringBuffer(30 * workstations_.size());
	firstNode_.printOn(this, buf);
        return buf.toString();
    }
    
}
