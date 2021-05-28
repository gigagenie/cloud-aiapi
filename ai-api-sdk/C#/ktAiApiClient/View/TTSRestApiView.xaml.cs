using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Windows;
using System.Windows.Controls;
using System.Windows.Data;
using System.Windows.Documents;
using System.Windows.Input;
using System.Windows.Media;
using System.Windows.Media.Imaging;
using System.Windows.Navigation;
using System.Windows.Shapes;

namespace AiApiTestClient.View
{
    /// <summary>
    /// TTSRestApiView.xaml에 대한 상호 작용 논리
    /// </summary>
    public partial class TTSRestApiView : UserControl
    {
        public TTSRestApiView()
        {
            InitializeComponent();
        }

        private void TextBox_TextChanged(object sender, TextChangedEventArgs e)
        {
            if (sender != null)
            {
                TextBox tb = sender as TextBox;

                if (DefaultText != null)
                {
                    if (string.IsNullOrEmpty(tb.Text))
                    {
                        DefaultText.Visibility = Visibility.Visible;
                    }
                    else
                    {
                        DefaultText.Visibility = Visibility.Hidden;
                    }
                }
            }
        }
    }
}
