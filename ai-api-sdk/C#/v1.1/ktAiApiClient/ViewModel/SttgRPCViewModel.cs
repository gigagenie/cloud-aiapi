using System;
using System.IO;
using System.Windows.Input;
using AiApiSDK;
using Microsoft.Toolkit.Mvvm.ComponentModel;
using Microsoft.Toolkit.Mvvm.DependencyInjection;
using Microsoft.Toolkit.Mvvm.Input;
using Microsoft.Win32;


namespace AiApiTestClient.ViewModel
{
    public class STTgRPCViewModel : ObservableRecipient
    {
        private STTgRPC sttClient;

        #region Properties
        private string audio_file_path;
        public string AudioFilePath
        {
            get => audio_file_path;
            set
            {
                audio_file_path = value;
                OnPropertyChanged("AudioFilePath");
            }
        }

        private int sttModelCode;
        public int SttModelCode
        {
            get => sttModelCode;
            set
            {
                sttModelCode = value;
                OnPropertyChanged("SttModelCode");
            }
        }

        private string sttmode;
        public string SttMode
        {
            get => sttmode;
            set
            {
                sttmode = value;
                OnPropertyChanged("SttMode");
            }
        }

        private int channelIndex = 0;
        public int ChannelIndex
        {
            get => channelIndex;
            set
            {
                channelIndex = value;
                OnPropertyChanged("ChannelIndex");
            }
        }

        private int samplerate = 16000;
        public int SampleRate
        {
            get => samplerate;
            set
            {
                samplerate = value;
                OnPropertyChanged("SampleRate");
            }
        }

        private string sampleformat;
        public string SampleFormat
        {
            get => sampleformat;
            set
            {
                sampleformat = value;
                OnPropertyChanged("SampleFormat");
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

        public STTgRPCViewModel()
        {
            sttClient = new STTgRPC();

            sttClient.onConnectGRPC += (args) => outputText += $"onConnectGRPC => errCode:{args.statusCode}, errMsg:{args.errorCode}\r\n";
            sttClient.onReadySTT += (sampleRate, channel, format) =>
            {
                outputText += $"onReadySTT => sampleRate:{sampleRate}, channel:{channel}, format:{format}\r\n";
            };

            sttClient.onError += (obj, args) =>
            {
                outputText += $"onError => errCode:{args.statusCode}, errMsg:{args.errorCode}\r\n";
            };

            sttClient.onSTTResult += (text, type, startTime, endTime) =>
            {
                outputText += $"onSTTResult => text:{text}, type:{type}, startTime:{startTime}, endTime:{endTime}\r\n";
            };

            sttClient.onStopSTT += (msg) => outputText += $"onStopSTT => sttPushTime{msg}\r\n";
            sttClient.onRelease += () => outputText += "onRelease\r\n";
        }

        #region Command
        public ICommand IFileOpenCommand
        {
            get => new RelayCommand(() =>
            {
                OpenFileDialog openFileDialog = new OpenFileDialog();
                openFileDialog.Filter = "Audio files (*.pcm;*.wav;*.mp3;*.ogg;*.m4a;*.flac)|*.pcm;*.wav;*.mp3;*.ogg;*.m4a;*.flac|All Files (*.*)|*.*";
                openFileDialog.InitialDirectory = AppDomain.CurrentDomain.BaseDirectory;
                if (openFileDialog.ShowDialog() == true)
                {
                    AudioFilePath = openFileDialog.FileName;

                    string ext = Path.GetExtension(AudioFilePath);
                }
            });
        }
        public ICommand IconnectGRPCCommand
        {
            get => new RelayCommand(() =>
            {
                string clientKey = Ioc.Default.GetService<AuthViewModel>().CLIENT_KEY;
                string clientId = Ioc.Default.GetService<AuthViewModel>().CLIENT_ID;
                string clientSecret = Ioc.Default.GetService<AuthViewModel>().CLIENT_SECRET;
                string service_url = Ioc.Default.GetService<AuthViewModel>().GRPC_SERVICE_URL;

                sttClient.setServiceURL(service_url);
                sttClient.setAuth(clientKey, clientId, clientSecret);

                sttClient.connectGRPC();
            });
        }

        public ICommand IstopSTTCommand
        {
            get => new RelayCommand(() =>
            {
                sttClient.stopSTT();
            });
        }

        public ICommand IsendAudioDataCommand
        {
            get => new RelayCommand(() =>
            {
                sttClient.startSTT(AudioFilePath, SttMode, SampleFormat, SampleRate, ChannelIndex + 1);
            });
        }
        public ICommand IsendAudioDataCommand2
        {
            get => new RelayCommand(() =>
            {
                sttClient.startSTT2(AudioFilePath, SttModelCode);
            });
        }

        public ICommand IClearOutputCommand { get => new RelayCommand(() => outputText = string.Empty); }
        #endregion
    }
}
