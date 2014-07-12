/* This is a port of the JGroups Draw demo
 * by Yann Sionneau <yann.sionneau@telecom-sudparis.eu>
 */

package net.sionneau.touchsurface;

import org.jgroups.util.Streamable;

import android.util.Log;

//import java.io.DataInputStream; //jgroups 2.x
//import java.io.DataOutputStream; //jgroups 2.x
import java.io.DataInput; //jgroups 3.x
import java.io.DataOutput; //jgroups 3.x
//import java.io.IOException;

/**
 * Encapsulates information about a draw command.
 * Used by the {@link Draw} and other demos.
 *
 */
public class DrawCommand implements Streamable {
	static final byte DRAW=1;
	static final byte CLEAR=2;
	byte mode;
	int x=0;
	int y=0;
	int r=0;
	int g=0;
	int b=0;

	public DrawCommand() { // needed for streamable
	}

	DrawCommand(byte mode) {
		this.mode=mode;
	}

	DrawCommand(byte mode, int x, int y, int r, int g, int b) {
		this.mode=mode;
		this.x=x;
		this.y=y;
		this.r=r;
		this.g=g;
		this.b=b;
	}

	//jroups 2.x
//	@Override
//	public void writeTo(DataOutputStream out) throws IOException {
//		StringBuilder ret=new StringBuilder();
//		ret.append("writeTo(): " + mode + ", location(" + x + "," + y + "), color(" + r + ","+ g + "," + b +")");
//		Log.i("TouchSurface", ret.toString());
//
//		out.writeByte(mode);
//		out.writeInt(x);
//		out.writeInt(y);
//		out.writeInt(r);
//		out.writeInt(g);
//		out.writeInt(b);
//	}
//	//jroups 2.x
//	@Override
//	public void readFrom(DataInputStream in) throws IOException, IllegalAccessException, InstantiationException {
//		mode=in.readByte();
//		x=in.readInt();
//		y=in.readInt();
//		r=in.readInt();
//		g=in.readInt();
//		b=in.readInt();
//
//		StringBuilder ret=new StringBuilder();
//		ret.append("readFrom(): " + mode + ", location(" + x + "," + y + "), color(" + r + ","+ g + "," + b +")");
//		Log.i("TouchSurface", ret.toString());
//
//	}


	public String toString() {
		StringBuilder ret=new StringBuilder();
		switch(mode) {
		case DRAW: ret.append("DRAW(" + x + ", " + y + ")");
		break;
		case CLEAR: ret.append("CLEAR");
		break;
		default:
			return "<undefined>";
		}
		Log.i("TouchSurface", ret.toString());
		return ret.toString();
	}

//	//jgroups 3.x
	@Override
	public void readFrom(DataInput in) throws Exception, IllegalAccessException, InstantiationException {
		mode=in.readByte();
		x=in.readInt();
		y=in.readInt();
		r=in.readInt();
		g=in.readInt();
		b=in.readInt();

		StringBuilder ret=new StringBuilder();
		ret.append("readFrom(): " + mode + ", location(" + x + "," + y + "), color(" + r + ","+ g + "," + b +")");
		Log.i("TouchSurface", ret.toString());

	}
	//jgroups 3.x
	@Override
	public void writeTo(DataOutput out) throws Exception {
		StringBuilder ret=new StringBuilder();
		ret.append("writeTo(): " + mode + ", location(" + x + "," + y + "), color(" + r + ","+ g + "," + b +")");
		Log.i("TouchSurface", ret.toString());

		out.writeByte(mode);
		out.writeInt(x);
		out.writeInt(y);
		out.writeInt(r);
		out.writeInt(g);
		out.writeInt(b);
	}

}
