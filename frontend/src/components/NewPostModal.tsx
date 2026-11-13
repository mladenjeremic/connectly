import { useState, type FormEvent } from "react";
import { toast } from "sonner";
import { api, errorMessage } from "../api/client";
import { btnPrimary, inputCls } from "../lib/styles";
import type { Post } from "../types";
import ImagePicker from "./ImagePicker";
import { ErrorText, Modal } from "./ui";
import { compressAll } from "../lib/image";

const MAX_IMAGES = 10;
const MAX_TEXT = 2000;

export default function NewPostModal({ onClose, onCreated }: { onClose: () => void; onCreated: (post: Post) => void }) {
  const [files, setFiles] = useState<File[]>([]);
  const [content, setContent] = useState("");
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState<string | null>(null);

  async function submit(e: FormEvent) {
    e.preventDefault();
    if (!content.trim() && files.length === 0) {
      setError("Dodaj tekst ili bar jednu fotografiju.");
      return;
    }
    setBusy(true);
    setError(null);
    try {
      const form = new FormData();
      form.append("content", content);
      (await compressAll(files)).forEach((f) => form.append("images", f));
      const { data } = await api.post<Post>("/posts", form);
      toast.success("Objava je objavljena.");
      onCreated(data);
      onClose();
    } catch (err) {
      setError(errorMessage(err));
    } finally {
      setBusy(false);
    }
  }

  return (
    <Modal title="Nova objava" onClose={onClose}>
      <form className="space-y-3 p-4" onSubmit={submit}>
        <ImagePicker files={files} onChange={setFiles} max={MAX_IMAGES} />
        <textarea className={inputCls} rows={4} maxLength={MAX_TEXT} placeholder="Napiši opis..."
          value={content} onChange={(e) => setContent(e.target.value)} />
        <div className="flex items-center justify-between">
          <span className="text-xs text-neutral-500">{content.length}/{MAX_TEXT}</span>
          <button className={btnPrimary} disabled={busy}>{busy ? "Objavljivanje..." : "Objavi"}</button>
        </div>
        {error && <ErrorText>{error}</ErrorText>}
      </form>
    </Modal>
  );
}
