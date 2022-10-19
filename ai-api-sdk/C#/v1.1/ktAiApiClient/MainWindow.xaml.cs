using System;
using System.Diagnostics;
using System.Windows;

namespace AiApiTestClient
{
    /// <summary>
    /// Interaction logic for MainWindow.xaml
    /// </summary>
    public partial class MainWindow : Window
    {
        public MainWindow()
        {
            InitializeComponent();

#if !DEBUG
            if(!IsCurrentProcessAdmin())
            {
                runWithAdmin();
            }
#endif
        }

        public bool IsCurrentProcessAdmin()
        {
            using var identity = System.Security.Principal.WindowsIdentity.GetCurrent();
            var principal = new System.Security.Principal.WindowsPrincipal(identity);
            return principal.IsInRole(System.Security.Principal.WindowsBuiltInRole.Administrator);
        }

        public void runWithAdmin()
        {
            var psi = new ProcessStartInfo
            {
                FileName = AppContext.BaseDirectory + @"\AiApiTestClient.exe",
                UseShellExecute = true,
                Verb = "runas",
            };

            Process.Start(psi);
            Environment.Exit(0);
        }
    }
}
