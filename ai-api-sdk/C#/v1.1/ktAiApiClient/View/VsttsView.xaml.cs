using System.Windows;
using System.Windows.Controls;

namespace AiApiTestClient.View
{
    /// <summary>
    /// VsttsView.xaml�� ���� ��ȣ �ۿ� ��
    /// </summary>
    public partial class VsttsView : UserControl
    {
        public VsttsView()
        {
            InitializeComponent();
        }

        private void TextBox_TextChanged2(object sender, TextChangedEventArgs e)
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
