using System;
using System.Collections.Generic;
using System.IO;
using System.Windows.Input;
using AiApiSDK;
using AiApiSDK.Model;
using AiApiSDK.Utils;
using Microsoft.Toolkit.Mvvm.ComponentModel;
using Microsoft.Toolkit.Mvvm.DependencyInjection;
using Microsoft.Toolkit.Mvvm.Input;
using Microsoft.Win32;

namespace AiApiTestClient.ViewModel
{
    public class ComboBoxItemString
    {
        public string valueString { get; set; }

        public ComboBoxItemString(string value)
        {
            valueString = value;
        }
    }
    public class STTViewModel : ObservableRecipient
    {
        private STT stt;

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

        private List<ComboBoxItemString> items;
        public List<ComboBoxItemString> Items
        {
            get
            {
                if(SttMode == 1)
                {
                    return new List<ComboBoxItemString>()
                    {
                        new ComboBoxItemString("raw"),
                        new ComboBoxItemString("wav"),
                        new ComboBoxItemString("mp3"),
                        new ComboBoxItemString("vor"),
                        new ComboBoxItemString("aac"),
                        new ComboBoxItemString("fla"),
                    };
                }
                else if(SttMode == 2)
                {
                    return new List<ComboBoxItemString>()
                    {
                        new ComboBoxItemString("mp3"),
                        new ComboBoxItemString("vor"),
                        new ComboBoxItemString("aac"),
                        new ComboBoxItemString("fla"),
                    };
                }

                return new List<ComboBoxItemString>(); 
            }
            set{
                items = value;
                OnPropertyChanged("Items");
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

        private int sttmode = 1;
        public int SttMode
        {
            get => sttmode;
            set
            {
                sttmode = value;
                OnPropertyChanged("SttMode");
                OnPropertyChanged("Items");
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

        private int samplerate;
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

        private string transactionid;
        public string transactionId
        {
            get => transactionid;
            set
            {
                transactionid = value;
                OnPropertyChanged("transactionId");
                OnPropertyChanged("IsEnableQuery");
            }
        }

        private bool isenablequery;
        public bool IsEnableQuery
        {
            get
            {
                if (!string.IsNullOrEmpty(transactionId))
                    return true;

                return false;
            }
            set
            {
                isenablequery = value;
                OnPropertyChanged("IsEnableQuery");
            }
        }

        public bool IsEncodingOptionEnable { get => Encoding == "raw" ? true : false; }

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

        public ICommand IRequestCommand
        {
            get => new RelayCommand(() =>
            {
                transactionId = string.Empty;

                string clientKey = Ioc.Default.GetService<AuthViewModel>().CLIENT_KEY;
                string clientId = Ioc.Default.GetService<AuthViewModel>().CLIENT_ID;
                string clientSecret = Ioc.Default.GetService<AuthViewModel>().CLIENT_SECRET;
                string service_url = Ioc.Default.GetService<AuthViewModel>().HTTP_SERVICE_URL;

                stt.setServiceURL(service_url);
                stt.setAuth(clientKey, clientId, clientSecret);

                RequestSTTResponse response = stt.requestSTT(AudioFilePath, SttMode, Language, Encoding, ChannelIndex + 1, SampleRate, sampleformat);
                processRequestSTTResponse(response);
            });
        }
        public ICommand IFileOpenCommand
        {
            get => new RelayCommand(() =>
            {
                OpenFileDialog openFileDialog = new OpenFileDialog()
                {
                    Filter = "Audio files (*.pcm;*.wav;*.mp3;*.ogg;*.m4a;*.flac)|*.pcm;*.wav;*.mp3;*.ogg;*.m4a;*.flac|All Files (*.*)|*.*",
                    InitialDirectory = AppDomain.CurrentDomain.BaseDirectory,
                };

                if (openFileDialog.ShowDialog() == true)
                {
                    AudioFilePath = openFileDialog.FileName;

                    string ext = Path.GetExtension(AudioFilePath);
                    Encoding = ext.Substring(1);
                }
            });
        }
        public ICommand IClearOutputCommand { get => new RelayCommand(() => outputText = string.Empty); }

        public ICommand IQuerySTTCommand
        {
            get => new RelayCommand(() =>
            {
                QuerySTTResponse response = stt?.querySTT(transactionId);
                processQueryResponse(response);
            });
        }
#endregion

        public STTViewModel()
        {
            stt ??= new();
        }

        private void processRequestSTTResponse(RequestSTTResponse response)
        {
            switch(response.statusCode)
            {
                case (int)System.Net.HttpStatusCode.OK:
                    {
                        outputtext += $"statusCode : {response.statusCode}, errorCode : {response.errorCode}\r\n";

                        foreach(VoiceRecognizeResponse data in response.result)
                        {
                            switch (data.resultType)
                            {
                                case "start":
                                {
                                    transactionId = data.transactionId;
                                    break;
                                }

                                case "text":
                                {
                                    outputText += $"text={data.sttResult.text}, startTime={data.sttResult.startTime}, endTime={data.sttResult.endTime}\r\n";
                                    break;
                                }

                                case "end":
                                {
                                    outputText += $"reqFileSize={data.sttInfo.reqFileSize}, transCodec={data.sttInfo.transCodec}, convFileSize={data.sttInfo.convFileSize}, sttProcTime={data.sttInfo.sttProcTime}";
                                    break;
                                }

                                case "err":
                                {
                                    string errDescription = ErrorCodeHelper.descriptionSTTErrcode(data.errCode);
                                    outputText += $"onError errCode:{data.errCode}, description:{errDescription}\r\n";
                                    break;
                                }
                            }
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

        private void processQueryResponse(QuerySTTResponse response)
        {
            switch (response.statusCode)
            {
                case (int)System.Net.HttpStatusCode.OK:
                    {
                        outputText += $"statusCode : {response.statusCode}, errorCode : {response.errorCode}\r\n";

                        if (!string.IsNullOrEmpty(response.sttStatus))
                            outputText += $"sttStatus : {response.sttStatus}\r\n";

                        if(response.sttResult != null)
                        {
                            foreach (var data in response.sttResult)
                            {
                                outputText += $"text={data.text}, startTime={data.startTime}, endTime={data.endTime}\r\n";
                            }
                        }

                        if(response.sttInfo != null)
                        {
                            outputText += $"reqFileSize={response.sttInfo.reqFileSize}, transCodec={response.sttInfo.transCodec}, convFileSize={response.sttInfo.convFileSize}, sttProcTime={response.sttInfo.sttProcTime}";
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
    }
}
