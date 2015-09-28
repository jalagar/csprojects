 // A client that uses the synthesizer package to replicate a plucked guitar string sound

public class improvisedmusic{

      public static void main(String[] args) {
          synthesizer.GuitarString [] melody= new synthesizer.GuitarString[37];
          synthesizer.GuitarString [] cmaj= new synthesizer.GuitarString[8];
          synthesizer.GuitarString [] gmaj= new synthesizer.GuitarString[8];
          synthesizer.GuitarString [] amin= new synthesizer.GuitarString[8];
          synthesizer.GuitarString [] fmaj= new synthesizer.GuitarString[8];
          for(int i=0; i<37; i+=1){
        	  melody[i]= new synthesizer.GuitarString(440.0 * Math.pow(2,(i-24.0)/12.0));
          }
          int cmaj_index=15;
          int gmaj_index=10;
          int amin_index=12;
          int fmaj_index=8;
          int [] majorscale= {2,2,1,2,2,2,1,2};
          int [] minorscale= {2,1,2,2,1,2,2,2};
          int m=0;
          while (m<8){
        	  cmaj[m]= new synthesizer.GuitarString
        			  (440.0 * Math.pow(2,(cmaj_index-24.0)/12.0));
        	  cmaj_index+= majorscale[m];
        	  gmaj[m]= new synthesizer.GuitarString
                (440.0 * Math.pow(2,(gmaj_index-24.0)/12.0));
        	  gmaj_index+= majorscale[m];
        	  amin[m]= new synthesizer.GuitarString
                (440.0 * Math.pow(2,(amin_index-24.0)/12.0));
        	  amin_index+= minorscale[m];
        	  fmaj[m]= new synthesizer.GuitarString
                (440.0 * Math.pow(2,(fmaj_index-24.0)/12.0));
        	  fmaj_index+= majorscale[m];
        	  m+=1;
          }
          synthesizer.GuitarString [] chords = new synthesizer.GuitarString[12];
          chords[0]= new synthesizer.GuitarString(440.0 * Math.pow(2,(3-24.0)/12.0));
          chords[1]= new synthesizer.GuitarString(440.0 * Math.pow(2,(7-24.0)/12.0));
          chords[2]= new synthesizer.GuitarString(440.0 * Math.pow(2,(10-24.0)/12.0));
          //gmaj
          chords[3]= new synthesizer.GuitarString(440.0 * Math.pow(2,(2-24.0)/12.0));
          chords[4]= new synthesizer.GuitarString(440.0 * Math.pow(2,(5-24.0)/12.0));
          chords[5]= new synthesizer.GuitarString(440.0 * Math.pow(2,(10-24.0)/12.0));
          //aminor
          chords[6]= new synthesizer.GuitarString(440.0 * Math.pow(2,(3-24.0)/12.0));
          chords[7]= new synthesizer.GuitarString(440.0 * Math.pow(2,(7-24.0)/12.0));
          chords[8]= new synthesizer.GuitarString(440.0 * Math.pow(2,(12-24.0)/12.0));
          //fmaj
          chords[9]= new synthesizer.GuitarString(440.0 * Math.pow(2,(3-24.0)/12.0));
          chords[10]= new synthesizer.GuitarString(440.0 * Math.pow(2,(8-24.0)/12.0));
          chords[11]= new synthesizer.GuitarString(440.0 * Math.pow(2,(12-24.0)/12.0));
          int z=10000;
          int c_index=0;
          int before = 0;
          int total_time = 0;
          while (total_time < 8){
              if(c_index==12){
                  c_index=0;
              }
              chords[c_index].pluck();
              c_index+=1;
              chords[c_index].pluck();
              c_index+=1;
              chords[c_index].pluck();
              c_index+=1;
              int time =0;
              while (time<8) {
                  int []x = {0,1,2,4,5};
                  int c = x[((int)(Math.random()*5))];
                  while (before==c) {
                      c = x[((int)(Math.random()*5))];
                  }
                  before = c;
                  if(c_index==3) {
                      cmaj[c].pluck();
                  } else if(c_index==6) {
                      gmaj[c].pluck();
                  } else if(c_index==9) {
                      amin[c].pluck();
                  } else {
                      fmaj[c].pluck(); 
                  }
                  while(z>0){
                      double sample=0;
                      if(c_index==3) {sample = cmaj[c].sample();}
                      else if(c_index==6) {sample = gmaj[c].sample();}
                      else if(c_index==9) {sample = amin[c].sample();}
                      else {sample = fmaj[c].sample();}
                      for (int i=0; i<12; i+=1){
                          sample+= chords[i].sample();
                      }
                      StdAudio.play(sample); 
                      for (int i=0; i<8;i +=1){
                          if (i%2 ==0) {
                              cmaj[i].tic();
                              gmaj[i].tic();
                              fmaj[i].tic();
                              amin[i].tic();
                          }
                      }
                      for (int i=0; i<12; i+=1){
                          chords[i].tic();
                      }
                      z-=1;
                  }
                  z=10000;
                  time+=1;
              }
          total_time += 1;
          }       
      }
}

