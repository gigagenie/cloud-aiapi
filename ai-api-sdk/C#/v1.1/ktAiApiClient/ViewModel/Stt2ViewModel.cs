using System;
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
    public class STT2ViewModel : ObservableRecipient
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

        private int sttModelcode = 3;
        public int SttModelcode
        {
            get => sttModelcode;
            set
            {
                sttModelcode = value;
                OnPropertyChanged("SttModelcode");
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

                RequestSTTResponse response = stt.requestSTT2(AudioFilePath, sttModelcode, Encoding);
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

        public STT2ViewModel()
        {
            stt ??= new();
        }

        private void processRequestSTTResponse(RequestSTTResponse response)
        {
            switch (response.statusCode)
            {
                case (int)System.Net.HttpStatusCode.OK:
                    {
                        outputtext += $"statusCode : {response.statusCode}, errorCode : {response.errorCode}\r\n";

                        foreach (VoiceRecognizeResponse data in response.result)
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

                        if (response.sttResult != null)
                        {
                            foreach (var data in response.sttResult)
                            {
                                outputText += $"text={data.text}, startTime={data.startTime}, endTime={data.endTime}\r\n";
                            }
                        }

                        if (response.sttInfo != null)
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

