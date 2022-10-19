﻿using System;
using System.IO;
using System.Windows.Input;
using AiApiSDK;
using AiApiSDK.Model;
using AiApiSDK.SupportOptions.TTS;
using Microsoft.Toolkit.Mvvm.ComponentModel;
using Microsoft.Toolkit.Mvvm.DependencyInjection;
using Microsoft.Toolkit.Mvvm.Input;

namespace AiApiTestClient.ViewModel
{
    public class TTSViewModel : ObservableRecipient
    {
        #region Properties
        private string text;
        public string Text
        {
            get => text;
            set
            {
                text = value;
                OnPropertyChanged("Text");
            }
        }

        private int speakerindex = (int)Speaker.Leedaseul;
        public int speakerIndex
        {
            get => speakerindex;
            set
            {
                speakerindex = value;
                OnPropertyChanged("speakerIndex");
            }
        }

        private int pitch = 100;
        public int PitchData
        {
            get => pitch;
            set
            {
                pitch = value;
                OnPropertyChanged("PitchData");
            }
        }

        private int speed = 100;
        public int SpeedData
        {
            get => speed;
            set
            {
                speed = value;
                OnPropertyChanged("SpeedData");
            }
        }

        private int volume = 100;
        public int VolumeData
        {
            get => volume;
            set
            {
                volume = value;
                OnPropertyChanged("VolumeData");
            }
        }

        private string language;
        public string Language
        {
            get => language;
            set
            {
                language = value;
                OnPropertyChanged("Language");
            }
        }

        private string encoding;
        public string Encoding
        {
            get => encoding;
            set
            {
                encoding = value;
                OnPropertyChanged("Encoding");
                OnPropertyChanged("IsEncodingOptionEnable");
            }
        }

        public bool IsEncodingOptionEnable
        {
            get => Encoding == "wav" ? true : false;
        }

        private int channel = 0;
        public int Channel
        {
            get => channel;
            set
            {
                channel = value;
                OnPropertyChanged("Channel");
            }
        }

        private int samplerate;
        public int sampleRate
        {
            get => samplerate;
            set
            {
                samplerate = value;
                OnPropertyChanged("sampleRate");
            }
        }

        private string sampleformat;
        public string sampleFormat
        {
            get => sampleformat;
            set
            {
                sampleformat = value;
                OnPropertyChanged("sampleFormat");
            }
        }

        private string outputtext;
        public string outputText
        {
            get => outputtext;
            set
            {
                outputtext = value;
                OnPropertyChanged("outputText");
            }
        }
        #endregion

        #region Command
        public ICommand IConvertCommand
        {
            get => new RelayCommand(() =>
            {
                TTS tts = new();

                string clientKey = Ioc.Default.GetService<AuthViewModel>().CLIENT_KEY;
                string clientId = Ioc.Default.GetService<AuthViewModel>().CLIENT_ID;
                string clientSecret = Ioc.Default.GetService<AuthViewModel>().CLIENT_SECRET;
                string service_url = Ioc.Default.GetService<AuthViewModel>().HTTP_SERVICE_URL;

                tts.setServiceURL(service_url);
                tts.setAuth(clientKey, clientId, clientSecret);

                RequestTTSResponse response = tts.requestTTS(Text, Language, PitchData, SpeedData, speakerIndex + 1, VolumeData, Encoding, Channel + 1, sampleRate, sampleFormat);
                processRequestTTSResponse(response);
            }
            );
         }

        public ICommand IClearOutputCommand { get => new RelayCommand(() => outputText = string.Empty); }
        #endregion
        public TTSViewModel()
        {
        }

        private void processRequestTTSResponse(RequestTTSResponse response) 
        {
            switch (response.statusCode)
            {
                case (int)System.Net.HttpStatusCode.OK:
                    {
                        outputText += $"statusCode : {response.statusCode}, errorCode : {response.errorCode}\r\n";

                        if(response.audioData != null)
                        {
                            makeAudiofile(response.audioData);
                        }
                        break;
                    }

                default:
                    {
                        outputText += $"statusCode : {response.statusCode}, errorCode : {response.errorCode}\r\n";
                        break;
                    }
            }
        }

        private void makeAudiofile(byte[] audioData)
        {
            try
            {
                string filePath = getAudioFilePath(Encoding);
                using FileStream fs = File.Create(filePath);
                fs.Write(audioData, 0, audioData.Length);

                outputText += $"save audio file : {filePath}";
            }
            catch (Exception ex)
            {
                Console.WriteLine(ex.Message);
                Console.WriteLine(ex.StackTrace);
            }
        }

        private string getAudioFilePath(string ext) =>
            Path.Combine(AppDomain.CurrentDomain.BaseDirectory, DateTime.Now.ToString("yyyyMMdd_HHmmss")) + $".{ext}";
    }
}
