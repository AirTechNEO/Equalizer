package tools;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.lang.Math;
import java.io.IOException;
import java.io.File;
import java.io.BufferedWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

import com.jcraft.jogg.Packet;
import com.jcraft.jogg.Page;
import com.jcraft.jogg.StreamState;
import com.jcraft.jogg.SyncState;
import com.jcraft.jorbis.Block;
import com.jcraft.jorbis.Comment;
import com.jcraft.jorbis.DspState;
import com.jcraft.jorbis.Info;

import org.jtransforms.fft.DoubleFFT_1D;
    
public class Equalizer{
	
	private boolean debug;
	ReentrantReadWriteLock RWlocker = new ReentrantReadWriteLock(true);
	Lock locker = RWlocker.writeLock();
	
	private String filepath;
	private String destpath;
	private FileInputStream fileStream;
    private File destFile;
	int currentByte;
	
	private Packet joggPacket = new Packet();
    private Page joggPage = new Page();
    private StreamState joggStreamState = new StreamState();
    private SyncState joggSyncState = new SyncState();

    private DspState jorbisDspState = new DspState();
    private Block jorbisBlock = new Block(jorbisDspState);
    private Comment jorbisComment = new Comment();
    private Info jorbisInfo = new Info();
    
    byte[] buffer = null;
    byte[] convertedBuffer;
    int bufferSize = 2052;
    int convertedBufferSize;
    int count = 0;
    int index = 0;
    
    private int nbBands=-1;
    private int refreshRate=-1;
    private double[] transfoBuffer;
    private ArrayList<Float>[] writingBuffer;
    private short[][] rawBuffer;
   
    private ArrayList<Short>[] convertBuffer;
    private BufferedWriter bufferedWriter;
	private FileWriter fileout;
    
    private SourceDataLine outputLine = null;
    private float[][][] pcmInfo;
    private int[] pcmIndex; 
    private int[] options=new int[1];

    /*public static void main(String[] args) {
    	Equalizer eq = new Equalizer();
		eq.setFilepath("C:\\Users\\box turbo\\Desktop\\scream.ogg");
		eq.setDestpath("C:\\Users\\box turbo\\Desktop\\equalizer.lsp");
		eq.setOptions(new int[] {0});
		eq.setNbBands(5);
		eq.setRefreshRate(5);
		eq.setDebug(true);
		try {
			eq.run();
		} catch (InterruptedException ex) {
			ex.printStackTrace();
		}
	}*/
	public Equalizer() {
		filepath="";
		destpath="";
		fileStream=null;
		debug=false;
	}
	private boolean initFileOutput(){
		if (destpath=="") {System.err.println("Output path unspecified.\n");}
		else {
			destFile= new File(destpath);
			if (destFile.isAbsolute()) {
				try { if (destFile.exists()) {
					if (destFile.isDirectory()) {destFile= new File(destpath+"\\equalizer.lsp");}//To solve the problem if destpath provided a path without extention.
						destpath = destpath.replaceFirst(destFile.getName()+"$","");//removes the name of the file in the filepath
						String changename=destFile.getName();
						changename = changename.substring(0, changename.lastIndexOf('.'));//Removes the extentions of the file name, omiting any previous extention.
						int i=2;
						while (destFile.exists()) {
							if(destFile.isDirectory()) {System.err.println("An error has occured during "
									+ "the creation of the file.");return false;}
							destFile = new File(destpath+changename+i+".lsp");
							i++;
						}
						destpath+=changename+i+".lsp";
					}
						destFile.createNewFile();
						fileout= new FileWriter(destFile);
						bufferedWriter= new BufferedWriter(fileout);
					return true;
				} catch (SecurityException s) {
					s.printStackTrace(); return false;
				} catch (IOException io) {
					io.printStackTrace(); return false;
				}
			}
			else {System.err.println("Incorrect output path parsed in, must be an absolute path.\n");}
		}
		return false;
	}
	private boolean initFileInput(){
	    try {
    		fileStream = new FileInputStream(filepath);
		} catch (FileNotFoundException e) {
			e.printStackTrace(); return false;
		}
	    return true;
	}
	private void initJOrbis()
    {
        debugOutput("Initializing JOrbis.");
        // Initialize SyncState
        joggSyncState.init();
        // Prepare the to SyncState internal buffer
        joggSyncState.buffer(bufferSize);
        // Fill the buffer with the data from SyncState's internal buffer. 
        //Note how the size of this new buffer is different from bufferSize.
        buffer = joggSyncState.data;
        debugOutput("Done initializing JOrbis.");
    }
	public boolean readHeader()
    {
        debugOutput("Starting to read the header.");
        /*
         * Variable used in loops below. While we need more data, we will
         * continue to read from the InputStream.
         */
        boolean needMoreData = true;
        /*
         * We will read the first three packets of the header. We start off by
         * defining packet = 1 and increment that value whenever we have
         * successfully read another packet.
         */
        int packet = 1;

        /*
         * While we need more data (which we do until we have read the three
         * header packets), this loop reads from the stream and has a big
         * <code>switch</code> statement which does what it's supposed to do in
         * regards to the current packet.
         */
        while(needMoreData)
        {
            // Read from the InputStream.
            try
            {
                count = fileStream.read(buffer, index, bufferSize);
            }
            catch(IOException exception)
            {
                System.err.println("Could not read from the input stream.");
                System.err.println(exception);
            }
            // We let SyncState know how many bytes we read.
            joggSyncState.wrote(count);
            /*
             * We want to read the first three packets. For the first packet, we
             * need to initialize the StreamState object and a couple of other
             * things. For packet two and three, the procedure is the same: we
             * take out a page, and then we take out the packet.
             */
            switch(packet)
            {
                // The first packet.
                case 1:
                {
                    // We take out a page.
                    switch(joggSyncState.pageout(joggPage))
                    {
                        // If there is a hole in the data, we must exit.
                        case -1:
                        {
                            System.err.println("There is a hole in the first "
                                + "packet data.");
                            return false;
                        }
                        // If we need more data, we break to get it.
                        case 0:
                        {
                            break;
                        }

                        /*
                         * We got where we wanted. We have successfully read the
                         * first packet, and we will now initialize and reset
                         * StreamState, and initialize the Info and Comment
                         * objects. Afterwards we will check that the page
                         * doesn't contain any errors, that the packet doesn't
                         * contain any errors and that it's Vorbis data.
                         */
                        case 1:
                        {
                            // Initializes and resets StreamState.
                            joggStreamState.init(joggPage.serialno());
                            joggStreamState.reset();

                            // Initializes the Info and Comment objects.
                            jorbisInfo.init();
                            jorbisComment.init();
                            // Check the page (serial number and stuff).
                            if(joggStreamState.pagein(joggPage) == -1)
                            {
                                System.err.println("We got an error while reading the first header page.");
                                return false;
                            }
                            /*
                             * Try to extract a packet. All other return values
                             * than "1" indicates there's something wrong.
                             */
                            if(joggStreamState.packetout(joggPacket) != 1)
                            {
                                System.err.println("We got an error while reading the first header packet.");
                                return false;
                            }
                            /*
                             * We give the packet to the Info object, so that it
                             * can extract the Comment-related information,
                             * among other things. If this fails, it's not
                             * Vorbis data.
                             */
                            if(jorbisInfo.synthesis_headerin(jorbisComment,
                                joggPacket) < 0)
                            {
                                System.err.println("We got an error while interpreting the first packet. Apparently, it's not Vorbis data.");
                                return false;
                            }

                            // We're done here, let's increment "packet".
                            packet++;
                            break;
                        }
                    }

                    /*
                     * Note how we are NOT breaking here if we have proceeded to
                     * the second packet. We don't want to read from the input
                     * stream again if it's not necessary.
                     */
                    if(packet == 1) break;
                }

                // The code for the second and third packets follow.
                case 2:    case 3:
                {
                    // Try to get a new page again.
                    switch(joggSyncState.pageout(joggPage))
                    {
                        // If there is a hole in the data, we must exit.
                        case -1:
                        {
                            System.err.println("There is a hole in the second "
                                + "or third packet data.");
                            return false;
                        }

                        // If we need more data, we break to get it.
                        case 0:
                        {
                            break;
                        }

                        /*
                         * Here is where we take the page, extract a packet and
                         * and (if everything goes well) give the information to
                         * the Info and Comment objects like we did above.
                         */
                        case 1:
                        {
                            // Share the page with the StreamState object.
                            joggStreamState.pagein(joggPage);

                            /*
                             * Just like the switch(...packetout...) lines
                             * above.
                             */
                            switch(joggStreamState.packetout(joggPacket))
                            {
                                // If there is a hole in the data, we must exit.
                                case -1:
                                {
                                    System.err
                                        .println("There is a hole in the first"
                                            + "packet data.");
                                    return false;
                                }

                                // If we need more data, we break to get it.
                                case 0:
                                {
                                    break;
                                }

                                // We got a packet, let's process it.
                                case 1:
                                {
                                    /*
                                     * Like above, we give the packet to the
                                     * Info and Comment objects.
                                     */
                                    jorbisInfo.synthesis_headerin(jorbisComment, joggPacket);
                                    // Increment packet.
                                    packet++;
                                    if(packet == 4)
                                    {
                                        /*
                                         * There is no fourth packet, so we will
                                         * just end the loop here.
                                         */
                                        needMoreData = false;
                                    }
                                    break;
                                }
                            }
                            break;
                        }
                    }
                    break;
                }
            }
            // We get the new index and an updated buffer.
            index = joggSyncState.buffer(bufferSize);
            buffer = joggSyncState.data;
            /*
             * If we need more data but can't get it, the stream doesn't contain
             * enough information.
             */
            if(count == 0 && needMoreData)
            {
                System.err.println("Not enough header data was supplied.");
                return false;
            }
        }
        debugOutput("Finished reading the header.");
        debugOutput("Nb channels:"+jorbisInfo.channels+" - Rate:"+jorbisInfo.rate+" - Version:"+jorbisInfo.version);
        debugOutput("Ratio value:"+jorbisInfo.rate/refreshRate);
        return true;
    }
	@SuppressWarnings("unchecked")
	public boolean initOption(){
        debugOutput("Initializing the sound system and conversion system.");
        // These buffers are used by the decoding and converting method.
        convertedBufferSize = bufferSize * 2;
        convertedBuffer = new byte[convertedBufferSize];
        rawBuffer= new short[jorbisInfo.channels][bufferSize];
        convertBuffer= new ArrayList[jorbisInfo.channels];
		writingBuffer= new ArrayList[nbBands];
        for (int i = 0; i < jorbisInfo.channels; i++){
        	convertBuffer[i]= new ArrayList<>(jorbisInfo.rate/refreshRate);
		}
		try {
			for (int i = 0; i < nbBands; i++) {
				writingBuffer[i]= new ArrayList<>((fileStream.available()/jorbisInfo.rate));
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
        // Initializes the DSP synthesis.
        jorbisDspState.synthesis_init(jorbisInfo);
        // Make the Block object aware of the DSP.
        jorbisBlock.init(jorbisDspState);
        // We need to know the channels and rate.
        int channels = jorbisInfo.channels;
        int rate = jorbisInfo.rate;
        // Creates an AudioFormat object and a DataLine.Info object.
        AudioFormat audioFormat = new AudioFormat((float) rate, 16, channels,
            true, false);
        DataLine.Info datalineInfo = new DataLine.Info(SourceDataLine.class,
            audioFormat, AudioSystem.NOT_SPECIFIED);
        // Check if the line is supported.
        if(!AudioSystem.isLineSupported(datalineInfo)){
            System.err.println("Audio file is not supported.");
            return false;
        }
        /*
         * Everything seems to be alright. Let's try to open a line with the
         * specified format and start the source data line.
         */
        try
        {
            outputLine = (SourceDataLine) AudioSystem.getLine(datalineInfo);
            outputLine.open(audioFormat);
        }
        catch(LineUnavailableException exception)
        {
            debugOutput("The audio file could not be opened due "
                + "to resource restrictions.");
            System.err.println(exception);
            return false;
        }
        catch(IllegalStateException exception)
        {
            debugOutput("The audio file is already open.");
            System.err.println(exception);
            return false;
        }
        catch(SecurityException exception)
        {
            debugOutput("The audio file could not be opened due "
                + "to security restrictions.");
            System.err.println(exception);
            return false;
        }
        // Start it.
        outputLine.start();
        //We create the PCM variables. The index is an array with the same length as the number of audio channels.
        pcmInfo = new float[1][][];
        pcmIndex = new int[jorbisInfo.channels];
        debugOutput("Done initializing the sound system and the conversion system.");
        return true;
    }
	public void readBody() throws InterruptedException
    {
        debugOutput("Converting the body.");
        /* Variable used in loops below, like in readHeader(). While we need
         * more data, we will continue to read from the InputStream.*/
        boolean needMoreData = true;
        while(needMoreData){
            switch(joggSyncState.pageout(joggPage)){
                // If there is a hole in the data, we just proceed.
                case -1: debugOutput("There is a hole in the data. We proceed.");
                // If we need more data, we break to get it.
                case 0: break;
                // If we have successfully checked out a page, we continue.
                case 1:{
                    // Give the page to the StreamState object.
                    joggStreamState.pagein(joggPage);
                    // If granulepos() returns "0", we don't need more data.
                    if(joggPage.granulepos() == 0){
                        needMoreData = false;
                        break;
                    }
                    // Here is where we process the packets.
                    processPackets: while(true){
                        switch(joggStreamState.packetout(joggPacket)){
                            // Is it a hole in the data?
                            case -1: debugOutput("There is a hole in the data, we continue though.");
                            // If we need more data, we break to get it.
                            case 0: break processPackets;
                            //If we have the data we need, we decode the packet.
                            case 1: decodeCurrentPacket();
                        }
                    }
                    //If the page is the end-of-stream, we don't need more data.
                    if(joggPage.eos() != 0) needMoreData = false;
                }
            }
            // If we need more data
            if(needMoreData)
            {
                // We get the new index and an updated buffer.
                index = joggSyncState.buffer(bufferSize);
                buffer = joggSyncState.data;
                // Read from the InputStream.
                try
                {
                    count = fileStream.read(buffer, index, bufferSize);
                }
                catch(Exception e)
                {
                    System.err.println(e);
                    return;
                }
                // We let SyncState know how many bytes we read.
                joggSyncState.wrote(count);
                // There's no more data in the stream.
                if(count == 0) needMoreData = false;
            }
        }
        debugOutput("Done converting the body. Now writing in the file.");
        writeEntireBuffer();
        debugOutput("Done writing in the file.");
    }
	private void decodeCurrentPacket() throws InterruptedException{
        int samples;
        // Check that the packet is a audio data packet etc.
        if(jorbisBlock.synthesis(joggPacket) == 0) jorbisDspState.synthesis_blockin(jorbisBlock); // Give the block to the DspState object.
        // We need to know how many samples to process.
        int range;
        //Get the PCM information and count the samples. And while these samples are more than zero...
        while((samples = jorbisDspState.synthesis_pcmout(pcmInfo, pcmIndex))>0){
            // We need to know for how many samples we are going to process.
            if(samples < convertedBufferSize) range = samples;
            else range = convertedBufferSize;
            // For each channel...
            for(int i = 0; i < jorbisInfo.channels; i++){
                // For every sample in our range...
                for(int j = 0; j < range; j++){
                    //Get the PCM value for the channel at the correct position.
                    int value = (int) (pcmInfo[0][i][pcmIndex[i] + j] * 32767);
                     //We make sure our value doesn't exceed or falls below +-32767.
                    if(value > 32767) value = 32767;
                    if(value < -32768) value = -32768;
                    //It the value is less than zero, we bitwise-or it with 32768 (which is 1000000000000000 = 10^15).
                    if(value < 0) value = value | 32768;
                    //We then put it inside the buffer so it can be converted later
                    rawBuffer[i][j]=(short) value;
                }
            }
            // Write the buffer to a .lsp file as an equalizer
            bufferFillUp();
            // Update the DspState object.
            jorbisDspState.synthesis_read(range);
        }
    }
	private void bufferFillUp() {
		/*We first check up if convertBuffer is ready to be loaded using the comparison 
		 * samplerate>refreshRate*convertBuffer.size() for maximum precision.
		 * If inferior, it just loads up in convertBuffer the content of rawBuffer */
        debugOutput("Treating package containing "+rawBuffer[0].length*jorbisInfo.channels+" short int");
		for(int i=0; i<rawBuffer[0].length;i++) {
			for (int j = 0; j < jorbisInfo.channels; j++) {
				convertBuffer[j].add(rawBuffer[j][i]);
			}
			debugOutput("Writing buffer is containing "+convertBuffer[0].size()*jorbisInfo.channels+" elements");
			//If convertBuffer's size matches the refresh rate, it's written inside the file after conversion.
			if (jorbisInfo.rate/refreshRate<=convertBuffer[0].size()) {
				debugOutput("Writing buffer filled up !");
				bufferConvert(options);
			}
			//Once it is done, the cycle begins anew.
		}	
	}
	protected void bufferConvert(int[] options) {
		if (!(nbBands<0 && convertBuffer[0].isEmpty())) {
			DoubleFFT_1D translator= new DoubleFFT_1D(convertBuffer[0].size());
			transfoBuffer = new double[convertBuffer[0].size()] ;
			if (options[0]<=0 || options[0]>jorbisInfo.channels) {
				for (int i = 0; i < convertBuffer[0].size(); i++) {
					for (int j = 0; j < jorbisInfo.channels; j++) {
						transfoBuffer[i]+=convertBuffer[j].get(0);
						convertBuffer[j].remove(0);
					}
					transfoBuffer[i]/=jorbisInfo.channels;
				}
			}
			else {
				for (int i = 0; i < convertBuffer[0].size(); i++) {
					transfoBuffer[i]+=convertBuffer[options[0]].get(0);
					convertBuffer[options[0]].remove(0);
				}
			}
			for (int i = 0; i < jorbisInfo.channels; i++) {
				convertBuffer[i].clear();
			}
			//Fourrier transformation, making transfoBuffer contain the equalized data
			translator.realForward(transfoBuffer);
			//Getting an average value for each bands
			for (int i = 0; i < nbBands; i++) {
				int nbvalinf=(transfoBuffer.length*i/nbBands);
				int nbvalsup=(transfoBuffer.length*(i+1)/nbBands);
				double sum=0.0;
				for (int val=nbvalinf; val < nbvalsup; val++) {
					sum+=transfoBuffer[val];
				}
				writingBuffer[i].add((float)(sum*200/((nbvalsup-nbvalinf)*32768)));
			}
		}
		else System.err.println("Number of bands not initialized");
	}
	protected void writeEntireBuffer(){
		try {
			DecimalFormat df = new DecimalFormat();
			df.setMaximumFractionDigits(3);
			String monowriter="";
			byte[] parent = new byte[16];
			new Random().nextBytes(parent);
			String parentid = new String(parent, Charset.forName("UTF-8"));
			String monowriterparent="{\"name\":\""+destFile.getName().substring(0, destFile.getName().lastIndexOf('.'))
					+"\",\"type\":\"6\",\"offset\":\"0\",\"objects\":[{\"id\":\""+parentid
					+"\",\"p\":\"\",\"d\":\"15\",\"ot\":0,\"st\":\"0\",\"name\":"
					+"\"parent\",\"akt\":3,\"ako\":0,\"o\":{\"x\":\"0\",\"y\":\"0\"},\"ed\":"
					+ "{\"bin\":\"0\",\"layer\":\"0\"},\"events\":{\"pos\":[{\"t\":\"0\",\"x\":"
					+ "\"0\",\"y\":\"0\"}],\"sca\":[{\"t\":\"0\",\"x\":\"1\",\"y\":\"1\"}],"
					+ "\"rot\":[{\"t\":\"0\",\"x\":\"0\"}],\"col\":[{\"t\":\"0\",\"x\":\"0\"}]}},";
			fileWrite(monowriterparent);
			for (int i = 0; i < nbBands; i++) {
				byte[] child = parent;
				double pos=givePos(i);
				child[14]=(byte)((child[14]+(i+1))%256);
				child[15]=(byte)((child[14]+4*(i+1))%256);
				String childid= new String(child,  StandardCharsets.UTF_8);
				monowriter="{\"id\":\""+childid+"\",\"pt\":\"111\",\"p\":\""+parentid+"\",\"d\":\"15\",\"ot\""
				+ ":0,\"st\":\"0\",\"name\":\"Band"+i+"\",\"akt\":3,\"ako\":0,\"o\":{\"x\":\"0\",\"y\":\"0\"},"
				+ "\"ed\":{\"bin\":\"1\",\"layer\":\""+i%15+"\"},\"events\":{\"pos\":[{\"t\":\"0\",\"x\":\""+pos
				+"\",\"y\":\"0\"}";
				locker.lock();
				fileWrite(monowriter);
				locker.unlock();
				for (int j = 0; j < writingBuffer[i].size(); j++) {
					monowriter=",{\"t\":\""+((float)(j+1)/refreshRate)
					+"\",\"x\":\""+pos+"\",\"y\":\""+Math.abs(writingBuffer[i].get(j)/2)+"\",\"ct\":\"InOutSine\"}";
					fileWrite(monowriter);
				}
				monowriter="],\"sca\":[{\"t\":\"0\",\"x\":\"1\",\"y\":\"0\"}";
				fileWrite(monowriter);
				for (int j = 0; j < writingBuffer[i].size(); j++) {
					monowriter=",{\"t\":\""+((float)(j+1)/refreshRate)
					+"\",\"x\":\"1\",\"y\":\""+Math.abs(writingBuffer[i].get(j))+"\",\"ct\":\"InOutSine\"}";
					fileWrite(monowriter);
				}
				monowriter="],\"rot\":[{\"t\":\"0\",\"x\":\"0\"}],\""
						+ "col\":[{\"t\":\"0\",\"x\":\"0\"}]}}";
				if (i < nbBands-1) monowriter+=",";
				fileWrite(monowriter);
			}
			fileWrite("]}");
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	private double givePos(int i) {
		if (nbBands%2==0) {
			if (i+1<=nbBands/2) return -((double)nbBands/2-i-1)*1.25-.125;
			else return (i+1)*1.25+.125;
		}
		else {
			if (i+1<((double)nbBands/2+.5)) return -((double)nbBands/2-i-1)*1.25;
			else if (i+1>((double)nbBands/2+.5)) return (i+1)*1.25+.125;
			else return 0;
		}
	}
	private void fileWrite(String monowriter) throws IOException, InterruptedException {
		locker.lock();
		bufferedWriter.write(monowriter);
		Thread.sleep(20);
		locker.unlock();
	}
	public void cleanUp(){
        debugOutput("Cleaning up.");
        // Clear the necessary JOgg/JOrbis objects.
        joggStreamState.clear();
        jorbisBlock.clear();
        jorbisDspState.clear();
        jorbisInfo.clear();
        joggSyncState.clear();
        // Closes the stream.
        try
        {	
        	bufferedWriter.close();
            if(fileStream != null) {
            	fileStream.close();
            }
        }
        catch(Exception e)
        {
        }

        debugOutput("Done cleaning up.");
    }
	public void run() throws InterruptedException {
		if (initFileInput()&&initFileOutput()) {
			if(fileStream == null){
        		System.err.println("We don't have an input stream and therefor "
        				+ "cannot continue.");
        		return;
			}
			// Initialize JOrbis and other components
			initJOrbis();
			//If we can read the header, we try to initiate the sound system. If we could initialize the sound system, we try to read the body.
			if(readHeader()&&initOption())
			{
				readBody();
			}
			// Afterwards, we clean up.
			cleanUp();
        }
	}
	private void debugOutput(String output)
    {
        if(debug) System.out.println("Debug: " + output);
    }
	public int getNbBands() {
		return nbBands;
	}
	public int getRefreshRate() {
		return refreshRate;
	}
	public void setNbBands(int nbBands) {
		this.nbBands = nbBands;
	}
	public void setRefreshRate(int refreshRate) {
		this.refreshRate = refreshRate;
	}
	public int getOption(int i) {
		if(i<0 || i>3) return options[0];
		else return options[i];
	}
	public void setOptions(int[] options) {
		this.options = options;
	}
	public String getFilepath() {
		return filepath;
	}
	public String getDestpath() {
		return destpath;
	}
	public void setDestpath(String destpath) {
		this.destpath = destpath;
	}
	public void setFilepath(String filepath) {
		this.filepath = filepath;
	}
	public void setDebug(boolean debug) {
		this.debug = debug;
	}
}
