using System;
using System.Windows.Input;
using AiApiSDK;
using Microsoft.Toolkit.Mvvm.ComponentModel;
using Microsoft.Toolkit.Mvvm.DependencyInjection;
using Microsoft.Toolkit.Mvvm.Input;
using Microsoft.Win32;

namespace AiApiTestClient.ViewModel
{
    public class GenieMemoViewModel : ObservableRecipient
    {
        private GenieMemo geniememo;

        #region Properties
        private string audio_file_path;
        public string AudioFilePathgenie
        {
            get => audio_file_path;
            set
            {
                audio_file_path = value;
                OnPropertyChanged("AudioFilePathgenie");
            }
        }

        private string selectgenie;
        public string Selectgenie
        {
            get => selectgenie;
            set
            {
                selectgenie = value;
                OnPropertyChanged("SelectgenieMemo");

            }
        }

        private string lastYN;
        public string LastYN
        {
            get => lastYN;
            set
            {
                lastYN = value;
                OnPropertyChanged("LastYN");
            }
        }

        private int callindex;
        public int Callindex
        {
            get => callindex;
            set
            {
                callindex = value;
                OnPropertyChanged("Callindex");

            }
        }

        private string callkey;
        public string Callkey
        {
            get => callkey;
            set
            {
                callkey = value;
                OnPropertyChanged("Callkey");
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

        public ICommand IRequestCommandgenie
        {
            get => new RelayCommand(() =>
            {
                string clientKey = Ioc.Default.GetService<AuthViewModel>().CLIENT_KEY;
                string clientId = Ioc.Default.GetService<AuthViewModel>().CLIENT_ID;
                string clientSecret = Ioc.Default.GetService<AuthViewModel>().CLIENT_SECRET;
                string service_url = Ioc.Default.GetService<AuthViewModel>().HTTP_SERVICE_URL;

                geniememo.setServiceURL(service_url);
                geniememo.setAuth(clientKey, clientId, clientSecret);

                string response;
                if (Selectgenie == "GenieMemo")
                {
                    response = geniememo.requestGenieMemo(AudioFilePathgenie, Selectgenie, LastYN, Callindex, callkey);
                }
                else
                {
                    response = geniememo.requestGenieMemoASYNC(AudioFilePathgenie, Selectgenie, LastYN, Callindex, callkey);
                }
                outputText += response;
            });
        }
        public ICommand IFileOpenCommandgenie
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
                    AudioFilePathgenie = openFileDialog.FileName;
                }
            });
        }
        public ICommand IClearOutputCommandgenie { get => new RelayCommand(() => outputText = string.Empty); }

        public ICommand IQueryGenieMemoCommand
        {
            get => new RelayCommand(() =>
            {
                string clientKey = Ioc.Default.GetService<AuthViewModel>().CLIENT_KEY;
                string clientId = Ioc.Default.GetService<AuthViewModel>().CLIENT_ID;
                string clientSecret = Ioc.Default.GetService<AuthViewModel>().CLIENT_SECRET;
                string service_url = Ioc.Default.GetService<AuthViewModel>().HTTP_SERVICE_URL;

                geniememo.setServiceURL(service_url);
                geniememo.setAuth(clientKey, clientId, clientSecret);

                string response = geniememo.queryGenieMemo(callkey);
                outputText += response;
            });
        }
        #endregion

        public GenieMemoViewModel()
        {
            geniememo ??= new();
        }
    }

}