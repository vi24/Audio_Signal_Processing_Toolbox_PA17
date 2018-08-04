function beat = beatDetection(songFile)
 %
 if nargin < 1, songFile = 'C:\Users\Matthias.Kaderli\Documents\GitHub\PAIT17_Audio_Signal_Processing_Toolbox\documents\audioFiles\tamborine_60bpm_1-4time_61beats_stereo.mp3'; end
   [audioArray, sampleRate] = audioread(songFile);
 
   
   [t,xcr,D,onsetenv,oesr] = tempo2(audioArray, sampleRate);
    beat = t;
 
end



