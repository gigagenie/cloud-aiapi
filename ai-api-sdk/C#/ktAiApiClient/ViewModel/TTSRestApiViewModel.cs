using AiApiSDK;
using AiApiSDK.Model;
using CommonServiceLocator;
using Microsoft.Toolkit.Mvvm.ComponentModel;
using Microsoft.Toolkit.Mvvm.DependencyInjection;
using Microsoft.Toolkit.Mvvm.Input;
using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Windows.Input;

namespace AiApiTestClient.ViewModel
{
    public class TTSRestApiViewModel : ObservableRecipient
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

        private int speakerindex = 1;
        public int speakerIndex
        {
            get => speakerindex;
            set
            {
                speakerindex = value;
                OnPropertyChanged("speakerIndex");
            }
        }

        private int pitch;
        public int Pitch
        {
            get => pitch;
            set
            {
                pitch = value;
                OnPropertyChanged("Pitch");
            }
        }

        private int speed;
        public int Speed
        {
            get => speed;
            set
            {
                speed = value;
                OnPropertyChanged("Speed");
            }
        }

        private int volume;
        public int Volume
        {
            get => volume;
            set
            {
                volume = value;
                OnPropertyChanged("Volume");
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

        private string samplefarmat;
        public string sampleFormat
        {
            get => samplefarmat;
            set
            {
                samplefarmat = value;
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
                string clientKey = Ioc.Default.GetService<AuthViewModel>().CLIENT_KEY;
                string clientId = Ioc.Default.GetService<AuthViewModel>().CLIENT_ID;
                string clientSecret = Ioc.Default.GetService<AuthViewModel>().CLIENT_SECRET;
                string service_url = Ioc.Default.GetService<AuthViewModel>().HTTP_SERVICE_URL;

                TTS tts = new ();

                if (tts.setAuth(clientKey, clientId, clientSecret))
                {
                    if (tts.setServiceURL(service_url))
                    {
                        RequestTTSResponse response = tts.requestTTS(Text, Pitch, Speed, speakerIndex + 1, -1/*Volume*/, Language, Encoding, Channel + 1, sampleRate, sampleFormat);
                        processRequestSTTResponse(response);
                    }
                }
            });
         }

        public ICommand IClearOutputCommand { get => new RelayCommand(() => outputText = string.Empty); }
        #endregion
        public TTSRestApiViewModel()
        {
        }

        private void processRequestSTTResponse(RequestTTSResponse response)
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
