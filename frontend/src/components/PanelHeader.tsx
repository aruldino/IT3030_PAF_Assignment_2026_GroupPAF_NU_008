type PanelHeaderProps = {
  title: string;
  text: string;
};

export function PanelHeader({ title, text }: PanelHeaderProps) {
  return (
    <div className="panel-header">
      <h2>{title}</h2>
      <p>{text}</p>
    </div>
  );
}
