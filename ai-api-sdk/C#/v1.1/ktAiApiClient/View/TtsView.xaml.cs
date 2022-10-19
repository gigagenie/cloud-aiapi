using System.Windows;
using System.Windows.Controls;

namespace AiApiTestClient.View
{
    /// <summary>
    /// TtsView.xaml에 대한 상호 작용 논리
    /// </summary>
    public partial class TtsView : UserControl
    {
        public TtsView()
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
