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
 *   Foundation, Inc. 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 *
 *   Copyright original Java version: 2004 Bart Du Bois, Serge Demeyer
 *   Copyright C++ version: 2006 Matthias Rieger, Bart Van Rompaey
 */
package lanSimulation.internals;

import java.io.IOException;
import java.io.Writer;

import lanSimulation.Network;

/**
 * A <em>Node</em> represents a single Node in a Local Area Network (LAN).
 * Several types of Nodes exist.
 */
public class Node {
	// enumeration constants specifying all legal node types
	/**
	 * A node with type NODE has only basic functionality.
	 */
	public static final byte NODE = 0;
	/**
	 * A node with type WORKSTATION may initiate requests on the LAN.
	 */
	public static final byte WORKSTATION = 1;
	/**
	 * A node with type PRINTER may accept packages to be printed.
	 */
	public static final byte PRINTER = 2;

	/**
	 * Holds the type of the Node.
	 */
	public byte type_;
	/**
	 * Holds the name of the Node.
	 */
	public String name_;
	/**
	 * Holds the next Node in the token ring architecture.
	 * 
	 * @see lanSimulation.internals.Node
	 */
	public Node nextNode_;

	/**
	 * Construct a <em>Node</em> with given #type and #name.
	 * <p>
	 * <strong>Precondition:</strong> (type >= NODE) & (type <= PRINTER);
	 * </p>
	 */
	public Node(byte type, String name) {
		assert (type >= NODE) & (type <= PRINTER);
		type_ = type;
		name_ = name;
		nextNode_ = null;
	}

	/**
	 * Construct a <em>Node</em> with given #type and #name, and which is linked
	 * to #nextNode.
	 * <p>
	 * <strong>Precondition:</strong> (type >= NODE) & (type <= PRINTER);
	 * </p>
	 */
	public Node(byte type, String name, Node nextNode) {
		assert (type >= NODE) & (type <= PRINTER);
		type_ = type;
		name_ = name;
		nextNode_ = nextNode;
	}

	public void printLogging(Writer report, Network network) throws IOException {
		report.write("\tNode '");
		report.write(name_);
		report.write("' passes packet on.\n");
		report.flush();
	}

	public boolean printDocument(Network network, Packet document, Writer report) {
		String author = "Unknown";
		String title = "Untitled";
		int startPos = 0, endPos = 0;

		if (type_ == Node.PRINTER) {
			try {
				if (document.message_.startsWith("!PS")) {
					startPos = document.message_.indexOf("author:");
					if (startPos >= 0) {
						endPos = document.message_.indexOf(".", startPos + 7);
						if (endPos < 0) {
							endPos = document.message_.length();
						}
						;
						author = document.message_.substring(startPos + 7,
								endPos);
					}
					;
					startPos = document.message_.indexOf("title:");
					if (startPos >= 0) {
						endPos = document.message_.indexOf(".", startPos + 6);
						if (endPos < 0) {
							endPos = document.message_.length();
						}
						;
						title = document.message_.substring(startPos + 6,
								endPos);
					}
					;
					network.printAccounting(report, author, title);
					report.write(">>> Postscript job delivered.\n\n");
					report.flush();
				} else {
					title = "ASCII DOCUMENT";
					if (document.message_.length() >= 16) {
						author = document.message_.substring(8, 16);
					}
					;
					network.printAccounting(report, author, title);
					report.write(">>> ASCII Print job delivered.\n\n");
					report.flush();
				}
				;
			} catch (IOException exc) {
				// just ignore
			}
			;
			return true;
		} else {
			try {
				report.write(">>> Destinition is not a printer, print job cancelled.\n\n");
				report.flush();
			} catch (IOException exc) {
				// just ignore
			}
			;
			return false;
		}
	}

	/**
	The #receiver is requested by #workstation to print #document on #printer.
	Therefore #receiver sends a packet across the token ring network, until either
	(1) #printer is reached or (2) the packet travelled complete token ring.
	<p><strong>Precondition:</strong> consistentNetwork() & hasWorkstation(workstation);</p>
	@param network TODO
	 * @param workstation Name of the workstation requesting the service.
	 * @param document Contents that should be printed on the printer.
	 * @param printer Name of the printer that should receive the document.
	 * @param report Stream that will hold a report about what happened when handling the request.
	 * @return Anwer #true when the print operation was succesful and #false otherwise
	 */
	    public boolean requestWorkstationPrintsDocument(Network network, String workstation, String document, String printer, Writer report) {
	        assert network.consistentNetwork() & network.hasWorkstation(workstation);
	
		try {
		    report.write("'");
		    report.write(workstation);
		    report.write("' requests printing of '");
		    report.write(document);
		    report.write("' on '");
		    report.write(printer);
		    report.write("' ...\n");
		} catch (IOException exc) {
		    // just ignore
		};
		
		boolean result = false;
	        Node startNode, currentNode;
	        Packet packet = new Packet(document, workstation, printer);
	        
	        startNode = (Node) network.workstations_.get(workstation);
	
		try {
		    startNode.printLogging(report, network);
		} catch (IOException exc) {
		    // just ignore
		};
	        currentNode = startNode.nextNode_;
	        while ((! atDestination(currentNode, packet))
		    & (! packet.origin_.equals(currentNode.name_))) {
		    try {
			currentNode.printLogging(report, network);
		    } catch (IOException exc) {
			// just ignore
		    };
		currentNode = currentNode.nextNode_;
	        };
	
		if (atDestination(currentNode, packet)) {
		    result = currentNode.printDocument(network, packet, report);
		} else {
		    try {
			report.write(">>> Destinition not found, print job cancelled.\n\n");
			report.flush();
		    } catch (IOException exc) {
			// just ignore
		    };
		    result = false;
		}
		
		return result;
	    }

	/**
	The #receiver is requested to broadcast a message to all nodes.
	Therefore #receiver sends a special broadcast packet across the token ring network,
	which should be treated by all nodes.
	<p><strong>Precondition:</strong> consistentNetwork();</p>
	@param network TODO
	 * @param report Stream that will hold a report about what happened when handling the request.
	 * @return Anwer #true when the broadcast operation was succesful and #false otherwise
	*/
	    public boolean requestBroadcast(Network network, Writer report) {
	        assert network.consistentNetwork();
	
		try {
		    report.write("Broadcast Request\n");
		} catch (IOException exc) {
		    // just ignore
		};
	
	        Node currentNode = this;
	        Packet packet = new Packet("BROADCAST", name_, name_);
	        do {
		    try {
			report.write("\tNode '");
			report.write(currentNode.name_);
			report.write("' accepts broadcase packet.\n");
			currentNode.printLogging(report, network);
		    } catch (IOException exc) {
			// just ignore
		    };
		    currentNode = currentNode.nextNode_;
	        } while (! atDestination(currentNode, packet));
	
		try {
		    report.write(">>> Broadcast travelled whole token ring.\n\n");
		} catch (IOException exc) {
		    // just ignore
		};
		return true;
	    }

	private boolean atDestination(Node currentNode, Packet packet) {
		return packet.destination_.equals(currentNode.name_);
	}

	/**
	Write a printable representation of #receiver on the given #buf.
	<p><strong>Precondition:</strong> isInitialized();</p>
	 * @param network TODO
	 * @param buf TODO
	*/
	    public void printOn (Network network, StringBuffer buf) {
	        assert network.isInitialized();
	        Node currentNode = this;
	        do {
	            switch (currentNode.type_) {
	                case Node.NODE:
	                    buf.append("Node ");
	                    buf.append(currentNode.name_);
	                    buf.append(" [Node]");
	                    break;
	                case Node.WORKSTATION:
	                    buf.append("Workstation ");
	                    buf.append(currentNode.name_);
	                    buf.append(" [Workstation]");
	                    break;
	                case Node.PRINTER:
	                    buf.append("Printer ");
	                    buf.append(currentNode.name_);
	                    buf.append(" [Printer]");
	                    break;
	                default:
	                    buf.append("(Unexpected)");;
	                    break;
	            };
	            buf.append(" -> ");
	            currentNode = currentNode.nextNode_;
	        } while (currentNode != this);
	        buf.append(" ... ");
	    }

	/**
	Write a HTML representation of #receiver on the given #buf.
	 <p><strong>Precondition:</strong> isInitialized();</p>
	 * @param network TODO
	 * @param buf TODO
	 */
	    public void printHTMLOn (Network network, StringBuffer buf) {
		assert network.isInitialized();
	
		buf.append("<HTML>\n<HEAD>\n<TITLE>LAN Simulation</TITLE>\n</HEAD>\n<BODY>\n<H1>LAN SIMULATION</H1>");
		Node currentNode = this;
		buf.append("\n\n<UL>");
		do {
		    buf.append("\n\t<LI> ");
		    switch (currentNode.type_) {
			case Node.NODE:
			    buf.append("Node ");
			    buf.append(currentNode.name_);
			    buf.append(" [Node]");
			    break;
			case Node.WORKSTATION:
			    buf.append("Workstation ");
			    buf.append(currentNode.name_);
			    buf.append(" [Workstation]");
			    break;
			case Node.PRINTER:
			    buf.append("Printer ");
			    buf.append(currentNode.name_);
			    buf.append(" [Printer]");
			    break;
			default:
			    buf.append("(Unexpected)");;
			    break;
		    };
		    buf.append(" </LI>");
		    currentNode = currentNode.nextNode_;
		} while (currentNode != this);
		buf.append("\n\t<LI>...</LI>\n</UL>\n\n</BODY>\n</HTML>\n");
	    }

	/**
	Write an XML representation of #receiver on the given #buf.
	<p><strong>Precondition:</strong> isInitialized();</p>
	 * @param network TODO
	 * @param buf TODO
	*/
	    public void printXMLOn (Network network, StringBuffer buf) {
		assert network.isInitialized();
	
		Node currentNode = this;
		buf.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n\n<network>");
		do {
		    buf.append("\n\t");
		    switch (currentNode.type_) {
			case Node.NODE:
			    buf.append("<node>");
			    buf.append(currentNode.name_);
			    buf.append("</node>");
			    break;
			case Node.WORKSTATION:
			    buf.append("<workstation>");
			    buf.append(currentNode.name_);
			    buf.append("</workstation>");
			    break;
			case Node.PRINTER:
			    buf.append("<printer>");
			    buf.append(currentNode.name_);
			    buf.append("</printer>");
			    break;
			default:
			    buf.append("<unknown></unknown>");;
			    break;
		    };
		    currentNode = currentNode.nextNode_;
		} while (currentNode != this);
		buf.append("\n</network>");
	    }

}