using System;
using System.IO;
using System.Windows.Input;
using AiApiSDK;
using AiApiSDK.Model;
using AiApiSDK.SupportOptions.VSDUB;
using Microsoft.Toolkit.Mvvm.ComponentModel;
using Microsoft.Toolkit.Mvvm.DependencyInjection;
using Microsoft.Toolkit.Mvvm.Input;
using Microsoft.Win32;

namespace AiApiTestClient.ViewModel
{
    public class VsdubViewModel : ObservableRecipient
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

        private int speakerindex = (int)Speaker.MinValue;
        public int speakerIndex
        {
            get => speakerindex;
            set
            {
                speakerindex = value;
                OnPropertyChanged("speakerIndex");
            }
        }

        private string voiceName;
        public string VoiceName
        {
            get => voiceName;
            set
            {
                voiceName = value;
                OnPropertyChanged("VoiceName");
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

        private string emotion;
        public string Emotion
        {
            get => emotion;
            set
            {
                emotion = value;
                OnPropertyChanged("Emotion");
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

        private string audiodata;
        public string audioData
        {
            get => audiodata;
            set
            {
                audiodata = value;
                OnPropertyChanged("audioData");
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
        public ICommand VsdubConvertCommand
        {
            get => new RelayCommand(() =>
            {
                VSDUB vsdub = new();

                string clientKey = Ioc.Default.GetService<AuthViewModel>().CLIENT_KEY;
                string clientId = Ioc.Default.GetService<AuthViewModel>().CLIENT_ID;
                string clientSecret = Ioc.Default.GetService<AuthViewModel>().CLIENT_SECRET;
                string service_url = Ioc.Default.GetService<AuthViewModel>().HTTP_SERVICE_URL;

                ErrorResponse resultAuth = new ErrorResponse(System.Net.HttpStatusCode.OK);
                ErrorResponse resultServiceURL = new ErrorResponse(System.Net.HttpStatusCode.OK);

                vsdub.setAuth(clientKey, clientId, clientSecret);
                vsdub.setServiceURL(service_url);

                RequestVSDUBResponse response = vsdub.requestVSDUB(Text, audioData, Language, VoiceName, Emotion, PitchData, SpeedData, speakerIndex, VolumeData, Encoding, sampleRate);
                processRequestVSDUBResponse(response);
            });
        }
        public ICommand IFileOpenCommand
        {
            get => new RelayCommand(() =>
            {
                OpenFileDialog openFileDialog = new OpenFileDialog()
                {
                    Filter = "Audio files (*.pcm;*.wav;*.mp3;*.vor;*.aac;*.fla)|*.pcm;*.wav;*.mp3;*.vor;*.aac;*.fla|All Files (*.*)|*.*",
                    InitialDirectory = AppDomain.CurrentDomain.BaseDirectory,
                };

                if (openFileDialog.ShowDialog() == true)
                {
                    audioData = openFileDialog.FileName;

                    string ext = Path.GetExtension(audioData);
                    Encoding = ext.Substring(1);
                }
            });
        }

        public ICommand IClearCommand { get => new RelayCommand(() => outputText = string.Empty); }
        #endregion
        public VsdubViewModel()
        {
        }

        private void processRequestVSDUBResponse(RequestVSDUBResponse response)
        {
            switch (response.statusCode)
            {
                case (int)System.Net.HttpStatusCode.OK:
                    {
                        outputText += $"statusCode : {response.statusCode}, errorCode : {response.errorCode}\r\n";

                        if (response.audioData != null)
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
