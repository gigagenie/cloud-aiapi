using Microsoft.Toolkit.Mvvm.ComponentModel;

namespace AiApiTestClient.ViewModel
{
    public class AuthViewModel : ObservableRecipient
    {
        #region Properties
        public string HTTP_SERVICE_URL
        {
            get => Settings.Default.HTTP_SERVICE_URL;
            set
            {
                Settings.Default.HTTP_SERVICE_URL = value;
                Settings.Default.Save();
                OnPropertyChanged("HTTP_SERVICE_URL");
            }
        }

        public string GRPC_SERVICE_URL
        {
            get => Settings.Default.GRPC_SERVICE_URL;
            set
            {
                Settings.Default.GRPC_SERVICE_URL = value;
                Settings.Default.Save();
                OnPropertyChanged("GRPC_SERVICE_URL");
            }
        }

        public string CLIENT_KEY
        {
            get => Settings.Default.CLIENT_KEY;
            set
            {
                Settings.Default.CLIENT_KEY = value;
                Settings.Default.Save();
                OnPropertyChanged("CLIENT_KEY");
            }
        }
        public string CLIENT_ID
        {
            get => Settings.Default.CLIENT_ID;
            set
            {
                Settings.Default.CLIENT_ID = value;
                Settings.Default.Save();
                OnPropertyChanged("CLIENT_ID");
            }
        }
        public string CLIENT_SECRET
        {
            get => Settings.Default.CLIENT_SECRET;
            set
            {
                Settings.Default.CLIENT_SECRET = value;
                Settings.Default.Save();
                OnPropertyChanged("CLIENT_SECRET");
            }
        }

        private bool isEnableSetAuth = true;
        public bool IsEnableSetAuth
        {
            get => isEnableSetAuth;
            set
            {
                isEnableSetAuth = value;
                OnPropertyChanged("IsEnableSetAuth");
            }
        }

        private bool isEnableServiceURL = true;
        public bool IsEnableServiceURL
        {
            get => isEnableServiceURL;
            set
            {
                isEnableServiceURL = value;
                OnPropertyChanged("IsEnableServiceURL");
            }
        }
        #endregion
    }
}
